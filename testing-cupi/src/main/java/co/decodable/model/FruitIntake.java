package co.decodable.model;

import java.io.Serializable;

public class FruitIntake implements Serializable {

    public String name;
    public int count;

    public FruitIntake() {
    }

    public FruitIntake(String name, int count) {
        this.name = name;
        this.count = count;
    }

    @Override
    public String toString() {
        return "FruitIntake [name=" + name + ", count=" + count + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + count;
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
        FruitIntake other = (FruitIntake) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (count != other.count)
            return false;
        return true;
    }

}
