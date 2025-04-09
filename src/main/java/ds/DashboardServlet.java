package ds;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.bson.Document;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private MongoDBManager dbManager;

    @Override
    public void init() throws ServletException {
        super.init();
        // Use the "stock_logs" collection for logging
        dbManager = new MongoDBManager("stock_logs");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Retrieve all logs, top 5 average score, bottom 5 average score
        List<Document> logs = dbManager.findDocuments();
        List<Document> top5 = dbManager.getAverageScoreTopN(5, true);
        List<Document> bottom5 = dbManager.getAverageScoreTopN(5, false);

        // Retrieve top 5 most frequent companies (by occurrence)
        List<Document> mostFrequent = dbManager.getMostFrequentCompaniesTopN(5);

        // Format Top 5 average score list
        StringBuilder top5Str = new StringBuilder();
        for (Document doc : top5) {
            String ticker = doc.getString("_id");
            Double avgScore = null;
            Object avgScoreObj = doc.get("avgScore");
            if (avgScoreObj instanceof Number) {
                avgScore = ((Number) avgScoreObj).doubleValue();
            }
            top5Str.append(ticker)
                    .append(" (Score: ")
                    .append(avgScore)
                    .append(")")
                    .append("<br/>");
        }

        // Format Bottom 5 average score list
        StringBuilder bottom5Str = new StringBuilder();
        for (Document doc : bottom5) {
            String ticker = doc.getString("_id");
            Double avgScore = null;
            Object avgScoreObj = doc.get("avgScore");
            if (avgScoreObj instanceof Number) {
                avgScore = ((Number) avgScoreObj).doubleValue();
            }
            bottom5Str.append(ticker)
                    .append(" (Score: ")
                    .append(avgScore)
                    .append(")")
                    .append("<br/>");
        }

        // Format Most Frequently Appearing Companies list
        StringBuilder frequentStr = new StringBuilder();
        for (Document doc : mostFrequent) {
            String ticker = doc.getString("_id");
            Integer count = doc.getInteger("count", 0);
            frequentStr.append(ticker)
                    .append(" (Count: ")
                    .append(count)
                    .append(")")
                    .append("<br/>");
        }

        // Set attributes for the JSP page
        req.setAttribute("logs", logs);
        req.setAttribute("top5", top5Str.toString());
        req.setAttribute("bottom5", bottom5Str.toString());
        req.setAttribute("mostFrequent", frequentStr.toString());

        // Forward the request to the dashboard JSP page
        req.getRequestDispatcher("/WEB-INF/dashboard.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
        dbManager.close();
    }
}