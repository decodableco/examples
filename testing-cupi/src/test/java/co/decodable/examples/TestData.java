package co.decodable.examples;

import java.util.Map;

import co.decodable.model.Nutritions;

public class TestData {

    public static final Map<String, Nutritions> FRUIT_NUTRITION_LOOKUP_TABLE = Map.of(
            "raspberry", new Nutritions(53.0, 0.7, 4.4, 12.0, 1.2),
            "melon", new Nutritions(34.0, 0.0, 8.0, 8.0, 0.0),
            "banana", new Nutritions(96.0, 0.2, 17.2, 22.0, 1.0));

    public static final Map<String, String> WIRE_MOCK_JSON_RESPONSES = Map.of(
            "raspberry",
            "{ \"nutritions\": { \"calories\": 53.0, \"fat\": 0.7, \"sugar\": 4.4, \"carbohydrates\": 12.0, \"protein\": 1.2 } }",
            "melon",
            "{ \"nutritions\": { \"calories\": 34.0, \"fat\": 0.0, \"sugar\": 8.0, \"carbohydrates\": 8.0, \"protein\": 0.0 } }",
            "banana",
            "{ \"nutritions\": { \"calories\": 96.0, \"fat\": 0.2, \"sugar\": 17.2, \"carbohydrates\": 22.0, \"protein\": 1.0 } }");

}
