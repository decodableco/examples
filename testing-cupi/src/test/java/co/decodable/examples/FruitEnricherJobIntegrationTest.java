package co.decodable.examples;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;
import co.decodable.sdk.pipeline.testing.PipelineTestContext;
import co.decodable.sdk.pipeline.testing.StreamRecord;
import co.decodable.sdk.pipeline.testing.TestEnvironment;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@Testcontainers
public class FruitEnricherJobIntegrationTest {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Container
    public RedpandaContainer broker = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

    @Test
    @DisplayName("testing FruitEnricherJob")
    public void shouldEnrichFruitIntakeEvents() throws Exception {
        TestEnvironment testEnvironment = TestEnvironment.builder()
                .withBootstrapServers(broker.getBootstrapServers())
                .withStreams(FruitEnricherJob.FRUIT_INTAKE_STREAM, FruitEnricherJob.FRUIT_INTAKE_ENRICHED_STREAM)
                .build();

        try (PipelineTestContext ctx = new PipelineTestContext(testEnvironment)) {
            // given
            var fruitIntakes = List.of(
                new FruitIntake("raspberry", 1),
                new FruitIntake("unknown", 10),
                new FruitIntake("melon", 2),
                new FruitIntake("banana", 3),
                new FruitIntake("", 10),
                new FruitIntake("nofruit", 10)
            );
            var intakePayloads = fruitIntakes.stream()
                                    .map(fi -> {
                                        try {
                                            return OBJECT_MAPPER.writeValueAsString(fi);
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.toList());
            intakePayloads.forEach(
                    payload -> ctx.stream(FruitEnricherJob.FRUIT_INTAKE_STREAM).add(new StreamRecord<>(payload)));

            // when
            ctx.runJobAsync(FruitEnricherJob::main);
            List<StreamRecord<String>> results = 
                ctx.stream(FruitEnricherJob.FRUIT_INTAKE_ENRICHED_STREAM)
                    .take(intakePayloads.size()).get(30, TimeUnit.SECONDS);

            // then
            var fruitEnrichedActual = 
                results.stream().map(sr -> {
                    try {
                        return OBJECT_MAPPER.readValue(sr.value(), FruitEnriched.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

            var fruitEnrichedExpected = fruitIntakes.stream().map(
                intake -> new FruitEnriched(intake,TestData.FRUIT_NUTRITION_LOOKUP_TABLE.get(intake.name))
            ).collect(Collectors.toList());
            
            assertThat(fruitEnrichedActual, hasSize(fruitEnrichedExpected.size()));
            assertThat(fruitEnrichedActual,containsInAnyOrder(fruitEnrichedExpected.toArray(new FruitEnriched[0])));
        }
    }

}
