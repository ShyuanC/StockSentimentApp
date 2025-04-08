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

        req.setAttribute("logs", logs);
        req.setAttribute("top5", top5);
        req.setAttribute("bottom5", bottom5);

        // Forward the request to the dashboard JSP page
        req.getRequestDispatcher("/WEB-INF/dashboard.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        super.destroy();
        dbManager.close();
    }
}