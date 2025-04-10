package co.decodable.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

public class MockNutritionEnricherTest {

    @RegisterExtension
    static WireMockExtension WIRE_MOCK = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .configureStaticDsl(true).build();

    @ParameterizedTest(name = "nutrition enrichment for valid fruit ''{0}'' with mocked api")
    @ValueSource(strings = { "raspberry", "melon", "banana" })
    public void testValidFruitWithMockedApi(String validFruitName) throws Exception {
        // given
        var fruitIntake = new FruitIntake(validFruitName, 1); 
        stubFor(
            get("/"+validFruitName).willReturn(
                jsonResponse(TestData.WIRE_MOCK_JSON_RESPONSES.get(validFruitName),200)
            )
        );
        
        // when
        var mockedEnricher = new HttpNutritionEnricher(WIRE_MOCK.getRuntimeInfo().getHttpBaseUrl());
        var enrichedActual = mockedEnricher.enrich(fruitIntake);

        // then
        var enrichedExpected = new FruitEnriched(fruitIntake, TestData.FRUIT_NUTRITION_LOOKUP_TABLE.get(validFruitName));
        assertEquals(enrichedExpected, enrichedActual);
    }

    @ParameterizedTest(name = "nutrition enrichment for invalid fruit ''{0}'' with mocked api")
    @ValueSource(strings = { "unknown", "nofruit", "" })
    public void testInvalidFruitWithMockedApi(String invalidFruitName) throws Exception {
        // given
        var fruitIntake = new FruitIntake("unknown", 1);
        stubFor(
            get("/"+invalidFruitName).willReturn(
                aResponse().withStatus(404)
            )
        );
        
        // when
        var mockedEnricher = new HttpNutritionEnricher(WIRE_MOCK.getRuntimeInfo().getHttpBaseUrl());
        var enrichedActual = mockedEnricher.enrich(fruitIntake);
        
        // then
        var enrichedExpected = new FruitEnriched(fruitIntake, TestData.FRUIT_NUTRITION_LOOKUP_TABLE.get(invalidFruitName));
        assertEquals(enrichedExpected, enrichedActual);
    }

}
