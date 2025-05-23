package ds;

import com.mongodb.client.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class MongoDBManager {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    // New constructor accepting a collection name
    public MongoDBManager(String collectionName) {
        String uri = "mongodb://ovendy0226:ds1234@cluster0-shard-00-00.9x2jk.mongodb.net:27017,cluster0-shard-00-01.9x2jk.mongodb.net:27017,cluster0-shard-00-02.9x2jk.mongodb.net:27017/?replicaSet=atlas-116scs-shard-0&ssl=true&authSource=admin&retryWrites=true&w=majority&appName=Cluster0";
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("myFirstDatabase");
        collection = database.getCollection(collectionName);
    }

    // Default constructor now uses "stock_logs" as the collection name
    public MongoDBManager() {
        this("stock_logs");
    }

    public void insertDocument(Document doc) {
        collection.insertOne(doc);
    }

    public List<Document> findDocuments() {
        List<Document> docs = new ArrayList<>();
        collection.find().into(docs);
        return docs;
    }

    // New method to aggregate average score per ticker and return top N (or bottom N if descending is false)
    public List<Document> getAverageScoreTopN(int topN, boolean descending) {
        List<Document> results = new ArrayList<>();
        collection.aggregate(List.of(
                // Unwind the array stored in api_response_data
                new Document("$unwind", "$api_response_data"),
                // Group by ticker and compute average sentiment_score
                new Document("$group", new Document("_id", "$api_response_data.ticker")
                        .append("avgScore", new Document("$avg", "$api_response_data.sentiment_score"))),
                // Sort by avgScore (descending if descending is true, ascending if false)
                new Document("$sort", new Document("avgScore", descending ? -1 : 1)),
                // Limit the number of documents to topN
                new Document("$limit", topN)
        )).into(results);
        return results;
    }

    public List<Document> getMostFrequentCompaniesTopN(int topN) {
        List<Document> results = new ArrayList<>();
        collection.aggregate(List.of(
                // Unwind the api_response_data array
                new Document("$unwind", "$api_response_data"),
                // Group by the ticker and count the occurrences
                new Document("$group", new Document("_id", "$api_response_data.ticker")
                        .append("count", new Document("$sum", 1))),
                // Sort by count in descending order
                new Document("$sort", new Document("count", -1)),
                // Limit the results to topN
                new Document("$limit", topN)
        )).into(results);
        return results;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}