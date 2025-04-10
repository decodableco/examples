package co.decodable.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;

public class HttpNutritionEnricherTest {

    static NutritionEnricher VALID_HTTP_ENRICHER;
    static NutritionEnricher INVALID_HTTP_ENRICHER;

    @BeforeAll
    static void initialize() {
        VALID_HTTP_ENRICHER = new HttpNutritionEnricher();
        INVALID_HTTP_ENRICHER = new HttpNutritionEnricher("https://unknown.enricher-api.com/v1/");
    }

    @ParameterizedTest(name = "nutrition enrichment for valid fruit ''{0}'' with valid api endpoint")
    @ValueSource(strings = { "raspberry", "melon", "banana" })
    public void testValidFruitWithExternalApi(String validFruitName) throws Exception {
        // given
        var fruitIntake = new FruitIntake(validFruitName, 1);
        // when
        var enrichedActual = VALID_HTTP_ENRICHER.enrich(fruitIntake);
        // then
        var enrichedExpected = new FruitEnriched(fruitIntake, TestData.FRUIT_NUTRITION_LOOKUP_TABLE.get(validFruitName));
        assertEquals(enrichedExpected, enrichedActual);
    }

    @ParameterizedTest(name = "nutrition enrichment for invalid fruit ''{0}'' with valid api endpoint")
    @ValueSource(strings = { "unknown", "nofruit", "" })
    public void testInvalidFruitWithExternalApi(String invalidFruitName) throws Exception {
        // given
        var fruitIntake = new FruitIntake(invalidFruitName, 1);
        // when
        var enrichedActual = VALID_HTTP_ENRICHER.enrich(fruitIntake);
        // then
        var enrichedExpected = new FruitEnriched(fruitIntake, TestData.FRUIT_NUTRITION_LOOKUP_TABLE.get(invalidFruitName));
        assertEquals(enrichedExpected, enrichedActual);
    }

    @ParameterizedTest(name = "nutrition enrichment for any fruit ''{0}'' with invalid api endpoint")
    @ValueSource(strings = { "raspberry", "unknown", "", "banana", "nofruit" })
    public void testAnyFruitWithNonExistantApiEndpoint(String anyFruitName) throws Exception {
        // given
        var fruitIntake = new FruitIntake(anyFruitName, 1);
        // when - then
        assertThrows(RuntimeException.class, () -> INVALID_HTTP_ENRICHER.enrich(fruitIntake));
    }

}
