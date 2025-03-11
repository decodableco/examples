package co.decodable.examples;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;
import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams(FruitEnricherJob.FRUIT_INTAKE_STREAM)
@SinkStreams(FruitEnricherJob.FRUIT_INTAKE_ENRICHED_STREAM)
public class FruitEnricherJob {

    public static final String FRUIT_INTAKE_STREAM = "fruit-intake";
    public static final String FRUIT_INTAKE_ENRICHED_STREAM = "fruit-intake-enriched";

    public static void main(String[] args) {
        try {
            var env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);

            DecodableStreamSource<FruitIntake> source = DecodableStreamSource.<FruitIntake>builder()
                    .withStreamName(FRUIT_INTAKE_STREAM)
                    .withDeserializationSchema(new JsonDeserializationSchema<>(FruitIntake.class))
                    .build();

            DecodableStreamSink<FruitEnriched> sink = DecodableStreamSink.<FruitEnriched>builder()
                    .withStreamName(FRUIT_INTAKE_ENRICHED_STREAM)
                    .withSerializationSchema(new JsonSerializationSchema<>())
                    .build();

            // create and read data stream from source
            env.fromSource(
                    source,
                    WatermarkStrategy.noWatermarks(),
                    "[stream-fruit-intake] fruit intake source")
                    // enrich the fruit intake events
                    .map(new HttpServiceEnricher())
                    // write the result into the sink i.e. the enriched stream
                    .sinkTo(sink);

            env.execute("fruit enricher cupi job with sdk");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
