package co.decodable.examples;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.List;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/local/embedding")
@Startup
public class LocalEmbeddingResource {

    EmbeddingModel embeddingModel;

    public LocalEmbeddingResource() {
        String pathToModel = "/deployments/model/all-mpnet-base-v2/model.onnx";
        String pathToTokenizer = "/deployments/model/all-mpnet-base-v2/tokenizer.json";
        PoolingMode poolingMode = PoolingMode.MEAN;
        embeddingModel = new OnnxEmbeddingModel(pathToModel, pathToTokenizer, poolingMode);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public List<Float> encode(String text) {
        Log.infov("🧮 calculate embedding locally for -> {0}",text);
        var result = embeddingModel.embed(text);
        Log.debugv("vector embedding: {0}",result.content().vectorAsList());
        return result.content().vectorAsList();
    }

}
