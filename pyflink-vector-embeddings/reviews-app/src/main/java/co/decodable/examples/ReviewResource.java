package co.decodable.examples;

import java.util.List;

import co.decodable.examples.entity.mysql.Review;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReviewResource {

    @GET
    public List<Review> listReviews() {
        return Review.listAll();
    }

    @POST
    @Transactional
    public Review addReview(Review review) {
        review.id=null;
        Review.persist(review);
        return review;
    }

}
