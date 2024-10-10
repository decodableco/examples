package co.decodable.examples;

import java.util.List;

import org.bson.Document;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import co.decodable.examples.entity.mongo.Review;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject
    @RestClient
    EmbeddingModelService embeddingModelService;

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    public List<Document> search(@QueryParam("text") String text) {
        var inputVector = embeddingModelService.calcVectorEmbedding(text);
        return Review.vectorSearch(inputVector,100,20);
    }

}
