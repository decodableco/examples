package co.decodable.examples;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.flink.api.common.functions.MapFunction;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;
import co.decodable.model.Nutritions;

public class HttpServiceEnricher implements MapFunction<FruitIntake, FruitEnriched> {

    public static final String API_SERVICE_ENDPOINT = "https://fruityvice.com/api/fruit/%s";

    private static transient ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static transient HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Override
    public FruitEnriched map(FruitIntake intake) throws Exception {
        try {
            var getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(API_SERVICE_ENDPOINT, intake.name)))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .GET()
                    .build();
            var response = HTTP_CLIENT.send(getRequest, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var nutritions = OBJECT_MAPPER.treeToValue(OBJECT_MAPPER.readTree(response.body()).get("nutritions"),Nutritions.class);
                return new FruitEnriched(intake, nutritions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FruitEnriched(intake, null);
    }

}
