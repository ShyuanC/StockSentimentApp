package ds;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bson.Document;
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

        List<Stock> sortedStocks = new ArrayList<>();
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            log.put("api_response_code", responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String json = response.toString();
                log.put("api_response_data", json);
                log.put("timestamp_response", new Date());

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Stock>>() {}.getType();
                sortedStocks = gson.fromJson(json, listType);
                sortedStocks.sort((a, b) -> Double.compare(b.sentiment_score, a.sentiment_score));

                List<Map<String, Object>> result = new ArrayList<>();
                for (Stock s : sortedStocks) {
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

    // Inner class representing a stock record in the API response
    static class Stock {
        int no_of_comments;
        String sentiment;
        double sentiment_score;
        String ticker;
    }
}