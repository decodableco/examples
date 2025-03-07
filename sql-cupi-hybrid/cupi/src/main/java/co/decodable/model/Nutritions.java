package co.decodable.model;

public class Nutritions {

    public double calories;
    public double fat;
    public double sugar;
    public double carbohydrates;
    public double protein;

    public Nutritions() {
    }

    public Nutritions(double calories, double fat, double sugar, double carbohydrates, double protein) {
        this.calories = calories;
        this.fat = fat;
        this.sugar = sugar;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
    }

    @Override
    public String toString() {
        return "Nutritions [calories=" + calories + ", fat=" + fat + ", sugar=" + sugar + ", carbohydrates="
                + carbohydrates + ", protein=" + protein + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(calories);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(fat);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(sugar);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(carbohydrates);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(protein);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        Nutritions other = (Nutritions) obj;
        if (Double.doubleToLongBits(calories) != Double.doubleToLongBits(other.calories))
            return false;
        if (Double.doubleToLongBits(fat) != Double.doubleToLongBits(other.fat))
            return false;
        if (Double.doubleToLongBits(sugar) != Double.doubleToLongBits(other.sugar))
            return false;
        if (Double.doubleToLongBits(carbohydrates) != Double.doubleToLongBits(other.carbohydrates))
            return false;
        if (Double.doubleToLongBits(protein) != Double.doubleToLongBits(other.protein))
            return false;
        return true;
    }

}
