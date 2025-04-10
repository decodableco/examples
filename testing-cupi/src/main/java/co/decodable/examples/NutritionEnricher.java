package co.decodable.examples;

import co.decodable.model.FruitEnriched;
import co.decodable.model.FruitIntake;

public interface NutritionEnricher {

    FruitEnriched enrich(FruitIntake intake);
    
}
