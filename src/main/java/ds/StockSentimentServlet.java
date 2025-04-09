package ds;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.bson.Document;

@WebServlet("/stocksentiment")
public class StockSentimentServlet extends HttpServlet {
    private MongoDBManager dbManager;

    @Override
    public void init() throws ServletException {
        super.init();
        dbManager = new MongoDBManager();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String date = req.getParameter("date");
        String userAgent = req.getHeader("User-Agent");

        Map<String, Object> log = new HashMap<>();
        log.put("date_requested", date);
        log.put("user_agent", userAgent);
        log.put("timestamp_request", new Date());

        String apiUrl = "https://tradestie.com/api/v1/apps/reddit";
        if (date != null && !date.isEmpty()) {
            apiUrl += "?date=" + date;
        }

        List<Stock> stocksList = new ArrayList<>();
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            log.put("api_response_code", responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
                in.close();

                String json = responseBuilder.toString();
                log.put("timestamp_response", new Date());

                // Parse the raw JSON response into a List of Stock objects
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Stock>>() {}.getType();
                stocksList = gson.fromJson(json, listType);

                // Instead of storing the raw list, convert each Stock into a Document so MongoDB can encode it properly
                List<Document> stockDocs = new ArrayList<>();
                for (Stock s : stocksList) {
                    Document stockDoc = new Document();
                    stockDoc.append("no_of_comments", s.no_of_comments);
                    stockDoc.append("sentiment", s.sentiment);
                    stockDoc.append("sentiment_score", s.sentiment_score);
                    stockDoc.append("ticker", s.ticker);
                    stockDocs.add(stockDoc);
                }
                log.put("api_response_data", stockDocs);

                // Sort the stocks list in descending order based on sentiment_score
                stocksList.sort((a, b) -> Double.compare(b.sentiment_score, a.sentiment_score));

                // Create a simplified result list for returning to the client
                List<Map<String, Object>> result = new ArrayList<>();
                for (Stock s : stocksList) {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("ticker", s.ticker);
                    obj.put("sentiment", s.sentiment);
                    obj.put("score", s.sentiment_score);
                    result.add(obj);
                }
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(result));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"API request failed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Server error\"}");
            log.put("error", e.getMessage());
        }

        // Log the request and response details to MongoDB
        dbManager.insertDocument(new Document(log));
    }

    @Override
    public void destroy() {
        super.destroy();
        dbManager.close();
    }

    // Inner class representing a stock record from the API response
    static class Stock {
        int no_of_comments;
        String sentiment;
        double sentiment_score;
        String ticker;
    }
}