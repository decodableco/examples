package co.decodable.examples;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.List;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hf/embedding")
public class HuggingFaceEmbeddingResource {

    @Inject
    EmbeddingModel embeddingModel;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public List<Float> encode(String text) {
        Log.infov("using HF API to calculate embedding for -> {0}",text);
        var result = embeddingModel.embed(text);
        Log.debugv("vector embedding: {0}",result.content().vectorAsList());
        return result.content().vectorAsList();
    }

}
