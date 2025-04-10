package co.decodable.model;

import java.io.Serializable;

public class FruitEnriched implements Serializable {

    public FruitIntake intake;
    public Nutritions nutritions;

    public FruitEnriched() {
    }

    public FruitEnriched(FruitIntake intake, Nutritions nutritions) {
        this.intake = intake;
        this.nutritions = nutritions;
    }

    @Override
    public String toString() {
        return "FruitEnriched [intake=" + intake + ", nutritions=" + nutritions + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((intake == null) ? 0 : intake.hashCode());
        result = prime * result + ((nutritions == null) ? 0 : nutritions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FruitEnriched other = (FruitEnriched) obj;
        if (intake == null) {
            if (other.intake != null)
                return false;
        } else if (!intake.equals(other.intake))
            return false;
        if (nutritions == null) {
            if (other.nutritions != null)
                return false;
        } else if (!nutritions.equals(other.nutritions))
            return false;
        return true;
    }

}
