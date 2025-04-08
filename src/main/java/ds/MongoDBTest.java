package ds;

import com.mongodb.client.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.ConnectionString;
import org.bson.Document;
import java.util.Scanner;

public class MongoDBTest {

    public static void main(String[] args) {
        // Initialize MongoDBManager (creates MongoClient internally)
        MongoDBManager dbManager = new MongoDBManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter a string to save to myFirstDatabase (type 'exit' to quit): ");
            String userInput = scanner.nextLine();
            if ("exit".equalsIgnoreCase(userInput)) break;

            // Create a document and insert it into MongoDB
            Document doc = new Document("test", userInput);
            dbManager.insertDocument(doc);

            System.out.println("Successfully inserted test into myFirstDatabase!");
            System.out.println("\nAll inputs stored in myFirstDatabase:");
            for (Document document : dbManager.findDocuments()) {
                System.out.println("- " + document.getString("test"));
            }
        }

        dbManager.close();
        scanner.close();
    }
}