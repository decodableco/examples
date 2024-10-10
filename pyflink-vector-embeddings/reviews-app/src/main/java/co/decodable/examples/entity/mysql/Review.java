package co.decodable.examples.entity.mysql;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Review extends PanacheEntity {
    public String itemId;
    public String reviewText;
}
