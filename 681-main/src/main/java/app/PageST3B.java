package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PageST3B implements Handler {

    public static final String URL = "/page3B.html";

    @Override
    public void handle(Context context) throws Exception {
        String html = "<html>";

        html = html + "<head>" + 
               "<title>Subtask 3.B</title>";
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "<style>"
                    + "body { display: flex; flex-direction: column; min-height: 100vh; }"
                    + ".content { flex: 1; text-align: center; width: 80%; margin: auto; }"
                    + "form { display: inline-block; text-align: left; }"
                    + ".footer { text-align: center; padding: 1rem; background-color: #f1f1f1; }"
                    + "table { margin: 0 auto; background-color: white; border-collapse: collapse; width: 60%; }"
                    + "table, th, td { border: 1px solid black; }"
                    + "th, td { padding: 10px; text-align: center; }"
                    + "</style>";
        html = html + "</head>";

        html = html + "<body>";

        html = html + """
            <div class='topnav'>
                <a href='/'>Homepage</a>
                <a href='mission.html'>About Us</a>
                <a href='page2A.html'>Change By country</a>
                <a href='page2B.html'>Change by food group</a>
                <a href='page3A.html'>Similarity Comparison</a>
                <a href='page3B.html'>Food commodities and groups</a>
            </div>
        """;

        html = html + """
            <div class='header'>
                <h1>Exploring Food Commodities and Groups</h1>
            </div>
        """;

        html = html + "<div class='content'>";

        JDBCConnection jdbc = new JDBCConnection();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBCConnection.DATABASE);
            stmt = conn.createStatement();

            String commodityQuery = "SELECT DISTINCT commodity FROM FoodLoss ORDER BY commodity";
            rs = stmt.executeQuery(commodityQuery);

            html = html + "<form action='/page3B.html' method='get'>";
            html = html + "<label for='commodity'>Commodity:</label>";
            html = html + "<select id='commodity' name='commodity'>";

            while (rs.next()) {
                String commodity = rs.getString("commodity");
                html = html + "<option value='" + commodity + "'>" + commodity + "</option>";
            }

            html = html + "</select><br><br>";
            html = html + "<label for='similarityCriteria'>Similarity Criteria:</label>";
            html = html + "<select id='similarityCriteria' name='similarityCriteria'>";
            html = html + "<option value='Ratio'>Ratio of food loss to food waste</option>";
            html = html + "<option value='HighestPercentage'>Highest percentage of food loss</option>";
            html = html + "<option value='LowestPercentage'>Lowest percentage of food loss</option>";
            html = html + "</select><br><br>";
            html = html + "<label for='numGroups'>Number of Similar Groups:</label>";
            html = html + "<input type='number' id='numGroups' name='numGroups' min='1' max='10' value='5'><br><br>";
            html = html + "<input type='submit' value='Submit'>";
            html = html + "</form>";

            rs.close();

            String commodity = context.queryParam("commodity");
            String similarityCriteria = context.queryParam("similarityCriteria");
            String numGroups = context.queryParam("numGroups");

            if (commodity != null && similarityCriteria != null && numGroups != null) {
                String sql = "";
                
                if (similarityCriteria.equals("Ratio")) {
                    sql = "SELECT FL.commodity, FL.cpc_code, AVG(FL.loss_percentage) AS avg_loss_percentage, C.descriptor AS Food_Group "
                        + "FROM FoodLoss FL "
                        + "JOIN CPC C ON SUBSTR(FL.cpc_code, 1, 3) = C.\"GROUP\" "
                        + "WHERE FL.commodity != '" + commodity + "' "
                        + "AND SUBSTR(FL.cpc_code, 1, 3) = (SELECT SUBSTR(cpc_code, 1, 3) FROM FoodLoss WHERE commodity = '" + commodity + "' LIMIT 1) "
                        + "GROUP BY FL.commodity, FL.cpc_code, C.descriptor "
                        + "ORDER BY ABS(AVG(FL.loss_percentage) - (SELECT AVG(loss_percentage) FROM FoodLoss WHERE commodity = '" + commodity + "')) ASC "
                        + "LIMIT " + numGroups;
                } else if (similarityCriteria.equals("HighestPercentage")) {
                    sql = "SELECT FL.commodity, FL.cpc_code, AVG(FL.loss_percentage) AS avg_loss_percentage, C.descriptor AS Food_Group "
                        + "FROM FoodLoss FL "
                        + "JOIN CPC C ON SUBSTR(FL.cpc_code, 1, 3) = C.\"GROUP\" "
                        + "WHERE FL.commodity != '" + commodity + "' "
                        + "AND SUBSTR(FL.cpc_code, 1, 3) = (SELECT SUBSTR(cpc_code, 1, 3) FROM FoodLoss WHERE commodity = '" + commodity + "' LIMIT 1) "
                        + "GROUP BY FL.commodity, FL.cpc_code, C.descriptor "
                        + "ORDER BY AVG(FL.loss_percentage) DESC "
                        + "LIMIT " + numGroups;
                } else if (similarityCriteria.equals("LowestPercentage")) {
                    sql = "SELECT FL.commodity, FL.cpc_code, AVG(FL.loss_percentage) AS avg_loss_percentage, C.descriptor AS Food_Group "
                        + "FROM FoodLoss FL "
                        + "JOIN CPC C ON SUBSTR(FL.cpc_code, 1, 3) = C.\"GROUP\" "
                        + "WHERE FL.commodity != '" + commodity + "' "
                        + "AND SUBSTR(FL.cpc_code, 1, 3) = (SELECT SUBSTR(cpc_code, 1, 3) FROM FoodLoss WHERE commodity = '" + commodity + "' LIMIT 1) "
                        + "GROUP BY FL.commodity, FL.cpc_code, C.descriptor "
                        + "ORDER BY AVG(FL.loss_percentage) ASC "
                        + "LIMIT " + numGroups;
                }



                rs = stmt.executeQuery(sql);

                html += "<table border='1'>";
                html += "<tr><th>Commodity</th><th>CPC Code</th><th>Avg Loss Percentage</th><th>Food Group</th></tr>";
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    html += "<tr>";
                    html += "<td>" + rs.getString("commodity") + "</td>";
                    html += "<td>" + rs.getString("cpc_code") + "</td>";
                    html += "<td>" + String.format("%.2f", rs.getDouble("avg_loss_percentage")) + "</td>";
                    html += "<td>" + rs.getString("Food_Group") + "</td>";
                    html += "</tr>";
                }
                html += "</table>";

                if (!hasResults) {
                    html += "<p>No results found for the query.</p>";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            html += "<p>Error: " + e.getMessage() + "</p>";
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }

        html += "</div>";

        html += """
            <div class='footer'>
            <div class='bottomnav'>
            <a href='/' class='bottomnav a'>Home</a>
            <a href='mission.html' class='bottomnav a'>About Us</a>
            <a href='page2A.html' class='bottomnav a'>Change By country</a>
            <a href='page2B.html' class='bottomnav a'>Change by food group</a>
            <a href='page3A.html' class='bottomnav a'>Similarity Comparison</a>
            <a href='page3B.html' class='bottomnav a'>Food commodities and groups</a>
            </div>
            </div>
        """;

        html += "</body></html>";
        
        context.html(html);
        
    }
}
