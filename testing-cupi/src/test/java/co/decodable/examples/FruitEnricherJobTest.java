package co.decodable.examples;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class FruitEnricherJobTest {

    @Test
    @DisplayName("testing FruitEnricherJob")
    public void shouldEnrichFruitIntakeEvents() throws Exception {
        // given
        var fruitIntakes = List.of(
            new FruitIntake("raspberry", 1),
            new FruitIntake("unknown", 10),
            new FruitIntake("melon", 2),
            new FruitIntake("banana", 3),
            new FruitIntake("", 10),
            new FruitIntake("nofruit", 10)
        );
        
        // when
        var dummySource = new TestCollectionSource<FruitIntake>(FruitIntake.class, fruitIntakes,
                TypeInformation.of(FruitIntake.class));
        var dummySink = new TestCollectionSink<FruitEnriched>();
        var dummyMapper = new FruitNutritionMapper(new FakeNutritionEnricher());
        
        var job = new FruitEnricherJob(dummySource, dummySink, dummyMapper);
        job.run();

        // then
        var fruitEnrichedActual = dummySink.getElements();
        var fruitEnrichedExpected = fruitIntakes.stream().map(
            intake -> new FruitEnriched(intake,TestData.FRUIT_NUTRITION_LOOKUP_TABLE.get(intake.name))
        ).collect(Collectors.toList());
        
        assertThat(fruitEnrichedActual, hasSize(fruitEnrichedExpected.size()));
        assertThat(fruitEnrichedActual, containsInAnyOrder(fruitEnrichedExpected.toArray(new FruitEnriched[0])));
    }

    @AfterEach
    void clearTestSink() {
        TestCollectionSink.clearElements();
    }

    public static class FakeNutritionEnricher implements NutritionEnricher, Serializable {

        @Override
        public FruitEnriched enrich(FruitIntake intake) {
            var nutritions = TestData.FRUIT_NUTRITION_LOOKUP_TABLE.get(intake.name);
            return new FruitEnriched(intake, nutritions);
        }
        
    }

    public static class TestCollectionSource<OUT> extends DataGeneratorSource<OUT> {

        public TestCollectionSource(Class<OUT> type, Collection<OUT> data, TypeInformation<OUT> typeInfo) {
            super(new FromListGeneratorFunction<>(type, data), data.size(), typeInfo);
        }

    }

    public static class FromListGeneratorFunction<OUT> implements GeneratorFunction<Long, OUT> {

        private final OUT[] array;

        @SuppressWarnings("unchecked")
        public FromListGeneratorFunction(Class<OUT> type, Collection<OUT> data) {
            this.array = data.toArray((OUT[]) Array.newInstance(type, data.size()));
        }

        @Override
        public OUT map(Long value) throws Exception {
            return value >= 0 && value.intValue() < array.length
                    ? array[value.intValue()]
                    : null;
        }

    }

    public static class TestCollectionSink<IN> implements Sink<IN> {

        private static final List<Object> ELEMENTS = Collections.synchronizedList(new ArrayList<>());

        @Override
        public SinkWriter<IN> createWriter(InitContext context) throws IOException {
            return new SinkWriter<IN>() {
                @Override
                public void write(IN element, Context context)
                        throws IOException, InterruptedException {
                    ELEMENTS.add(element);
                }

                @Override
                public void flush(boolean endOfInput) throws IOException, InterruptedException {
                    // no-op
                }

                @Override
                public void close() throws Exception {
                    // no-op
                }
            };
        }

        @SuppressWarnings("unchecked")
        public List<IN> getElements() {
            return (List<IN>) ELEMENTS;
        }

        public static void clearElements() {
            ELEMENTS.clear();
        }

    }

}
