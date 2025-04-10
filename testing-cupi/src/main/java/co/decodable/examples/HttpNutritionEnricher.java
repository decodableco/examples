package co.decodable.examples;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;
import co.decodable.model.Nutritions;

public class HttpNutritionEnricher implements NutritionEnricher, Serializable {

    public static final String API_SERVICE_ENDPOINT = "https://fruityvice.com/api/fruit/";

    private static final transient ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final transient HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private String apiServiceEndpoint;

    public HttpNutritionEnricher() {
        this.apiServiceEndpoint = API_SERVICE_ENDPOINT;
    }

    public HttpNutritionEnricher(String apiServiceEnpoint) {
        Objects.requireNonNull(apiServiceEnpoint);
        this.apiServiceEndpoint = 
            apiServiceEnpoint.endsWith("/")
            ? apiServiceEnpoint
            : apiServiceEnpoint + "/";
    }

    @Override
    public FruitEnriched enrich(FruitIntake intake) {
        try {
            var getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(apiServiceEndpoint+"%s", intake.name)))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .GET()
                    .build();
            var response = HTTP_CLIENT.send(getRequest, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var nutritions = OBJECT_MAPPER.treeToValue(OBJECT_MAPPER.readTree(response.body()).get("nutritions"),
                        Nutritions.class);
                return new FruitEnriched(intake, nutritions);
            }
            return new FruitEnriched(intake, null);
        } catch (Exception e) {
            throw new RuntimeException("failed to enrich fruit intake",e);
        }
    }

}
