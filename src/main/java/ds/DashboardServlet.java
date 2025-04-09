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
        // Retrieve all log documents from MongoDB
        List<Document> logs = dbManager.findDocuments();
        // Retrieve top 5 tickers by average score
        List<Document> top5 = dbManager.getAverageScoreTopN(5, true);
        // Retrieve bottom 5 tickers by average score
        List<Document> bottom5 = dbManager.getAverageScoreTopN(5, false);

        // Convert top5 list into a displayable HTML string in the format: TICKER (Score: VALUE)
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

        // Convert bottom5 list into a similar HTML string
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

        // Set attributes for the JSP page
        req.setAttribute("logs", logs);
        req.setAttribute("top5", top5Str.toString());
        req.setAttribute("bottom5", bottom5Str.toString());

        // Forward the request to the dashboard JSP page
        req.getRequestDispatcher("/WEB-INF/dashboard.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
        dbManager.close();
    }
}