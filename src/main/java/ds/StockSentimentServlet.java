package ds;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.bson.Document;

@WebServlet(name = "StockSentimentServlet", urlPatterns = {"/stocksentiment"})
public class StockSentimentServlet extends HttpServlet {
    private MongoDBManager dbManager;

    @Override
    public void init() throws ServletException {
        super.init();
        dbManager = new MongoDBManager("stock_logs");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Retrieve input parameter "date" from the mobile app request.
        String date = req.getParameter("date");

        // Validate mobile app input: if 'date' is missing or empty, return error.
        if (date == null || date.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Missing or empty date parameter.\"}");
            return;
        }

        // Get additional request information (e.g., user agent) for logging.
        String userAgent = req.getHeader("User-Agent");
        Map<String, Object> log = new HashMap<>();
        log.put("date_requested", date);
        log.put("user_agent", userAgent);
        log.put("timestamp_request", new Date());

        // Build the third-party API URL with the provided date.
        String apiUrl = "https://tradestie.com/api/v1/apps/reddit?date=" + date;

        List<Stock> stocksList = new ArrayList<>();
        try {
            // Establish an HTTP connection to the third-party API.
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            log.put("api_response_code", responseCode);

            // Handle third-party API unavailable (non-200 HTTP response)
            if (responseCode != HttpURLConnection.HTTP_OK) {
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Third-party API returned response code " + responseCode + ".\"}");
                log.put("error", "Third-party API returned non-OK response: " + responseCode);
                return;
            }

            // Read the API response.
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();
            String json = responseBuilder.toString();
            log.put("timestamp_response", new Date());

            // Parse the JSON response into a List of Stock objects.
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Stock>>() {}.getType();
            try {
                stocksList = gson.fromJson(json, listType);
            } catch (Exception e) {
                // Handle invalid data returned from the third-party API.
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Invalid data received from third-party API.\"}");
                log.put("error", "JSON parsing error: " + e.getMessage());
                return;
            }

            // Handle case when the API returns empty or null data.
            if (stocksList == null || stocksList.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"No stock data available for the given date.\"}");
                log.put("error", "Empty stock data received.");
                return;
            }

            // Sort the stock list in descending order based on sentiment_score.
            stocksList.sort((a, b) -> Double.compare(b.sentiment_score, a.sentiment_score));

            // Create a simplified list to return only the necessary information to the mobile app.
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

        } catch (IOException e) {
            // Handle network failures, such as inability to reach the third-party API.
            resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Network error: Unable to reach the third-party API.\"}");
            log.put("error", "Network error: " + e.getMessage());
            return;
        } catch (Exception e) {
            // Catch-all for any unexpected server-side errors.
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Server encountered an error: " + e.getMessage() + "\"}");
            log.put("error", "Unexpected error: " + e.getMessage());
            return;
        } finally {
            // Log the request details (including any errors) to MongoDB.
            try {
                dbManager.insertDocument(new Document(log));
            } catch (Exception e) {
                e.printStackTrace(); // Optionally log if MongoDB logging fails.
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        dbManager.close();
    }

    // Inner class representing a stock record from the API response.
    static class Stock {
        int no_of_comments;
        String sentiment;
        double sentiment_score;
        String ticker;
    }
}