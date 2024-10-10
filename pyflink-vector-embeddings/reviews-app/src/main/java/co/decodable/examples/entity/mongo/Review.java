package co.decodable.examples.entity.mongo;

import java.util.List;
import java.util.stream.StreamSupport;

import org.bson.Document;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.Id;

@MongoEntity(collection = "review_embeddings")
public class Review extends PanacheMongoEntityBase {
    
    @Id
    public Long id;
    public String itemId;
    public String reviewText;
    public List<Float> embedding;

    public static List<Document> vectorSearch(List<Float> queryVector,int numCandidates, int limit) {
        var aggIter = mongoCollection().aggregate(
            List.of(
                new Document("$vectorSearch",
                    new Document("index","vector_index")
                        .append("path", "embedding")
                        .append("queryVector",queryVector)
                        .append("numCandidates",numCandidates)
                        .append("limit", limit)),
                new Document("$project", 
                    new Document("_id", 1L)
                            .append("itemId", 1L)
                            .append("reviewText", 1L)
                            .append("score", new Document("$meta", "vectorSearchScore")))
            ),
            Document.class
        );
        return StreamSupport.stream(aggIter.spliterator(), false).toList();
    }

}
