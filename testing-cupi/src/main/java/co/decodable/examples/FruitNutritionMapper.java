package co.decodable.examples;

import org.apache.flink.api.common.functions.MapFunction;

import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;

public class FruitNutritionMapper implements MapFunction<FruitIntake, FruitEnriched> {

    private final NutritionEnricher nutritionEnricher;
    
    public FruitNutritionMapper() {
        this.nutritionEnricher = new HttpNutritionEnricher();
    }
    
    public FruitNutritionMapper(NutritionEnricher nutritionEnricher) {
        this.nutritionEnricher = nutritionEnricher;
    }

    @Override
    public FruitEnriched map(FruitIntake intake) throws Exception {
        return nutritionEnricher.enrich(intake);
    }

}
