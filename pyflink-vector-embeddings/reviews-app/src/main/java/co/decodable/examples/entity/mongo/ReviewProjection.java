package co.decodable.examples.entity.mongo;

import io.quarkus.mongodb.panache.common.ProjectionFor;

@ProjectionFor(Review.class)
public class ReviewProjection {
    
    public Long id;
    public String itemId;
    public String reviewText;

}
