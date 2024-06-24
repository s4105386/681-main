package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 * Example Index HTML class using Javalin
 * <p>
 * Generate a static HTML page using Javalin
 * by writing the raw HTML into a Java String object
 *
 * @auther Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @auther Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 * @auther Halil Ali, 2024. email: halil.ali@rmit.edu.au
 */

public class PageST2B implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page2B.html";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Head information
        html = html + "<head>" + 
               "<title>Subtask 2.B</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "<style>"
                    + "body { display: flex; flex-direction: column; min-height: 100vh; }"
                    + ".content { flex: 1; text-align: center; width: 80%; margin: auto; }"
                    + "form { display: inline-block; text-align: left; }"
                    + ".footer { text-align: center; padding: 1rem; background-color: #f1f1f1; }"
                    + "</style>";
        html = html + "</head>";

        // Add the body
        html = html + "<body>";

        // Add the topnav
        html = html + """
            <div class='topnav'>
                <a href='/'>Homepage</a>
                <a href='mission.html'>About Us</a>
                <a href='page2A.html'>Change By country</a>
                <a href='page2B.html'>Change by food group</a>
                <a href='page3A.html'>Similarity Comparison</a>
                <a href='page3B.html'>Food commodoties and groups</a>
            </div>
        """;

        // Add header content block
        html = html + """
            <div class='header'>
                <h1>Focused view of loss/waste change by Food Group</h1>
            </div>
        """;

        // Add Div for page Content
        html = html + "<div class='content'>";

        // Use JDBCConnection class to connect to the database
        JDBCConnection jdbc = new JDBCConnection();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Establish a connection to the database
            conn = DriverManager.getConnection(JDBCConnection.DATABASE);
            stmt = conn.createStatement();

            // Query to get the list of descriptors
            String descriptorQuery = "SELECT DESCRIPTOR FROM CPC WHERE LENGTH(\"GROUP\") = 3 AND \"GROUP\" GLOB '[0-9][0-9][0-9]'";
            rs = stmt.executeQuery(descriptorQuery);

            // Generate the form with a drop-down menu for descriptors
            html = html + "<form action='/page2B.html' method='get'>";
            html = html + "<label for='startYear'>Start Year:</label>";
            html = html + "<input type='text' id='startYear' name='startYear'><br><br>";
            html = html + "<label for='endYear'>End Year:</label>";
            html = html + "<input type='text' id='endYear' name='endYear'><br><br>";
            html = html + "<label for='foodGroup'>Food Group:</label>";
            html = html + "<select id='foodGroup' name='foodGroup'>";
            html = html + "<option value='All'>All</option>"; // Add option for All

            // Populate the drop-down menu with descriptors from the database
            while (rs.next()) {
                String descriptor = rs.getString("DESCRIPTOR");
                html = html + "<option value='" + descriptor + "'>" + descriptor + "</option>";
            }

            html = html + "</select><br><br>";
            html = html + "<label for='sortingOrder'> Sort By:</label>";
            html = html + "<select id='sortingOrder' name='sortingOrder'>";
            html = html + "<option value='None'></option>";
            html = html + "<option value='StartYearAverageLoss'>Start year ascending loss</option>";
            html = html + "<option value='EndYearAverageLoss'>End year ascending loss</option>";
            html = html + "<option value='StartYearAverageLoss DESC'>Start year descending loss</option>";
            html = html + "<option value='EndYearAverageLoss DESC'>End year descending loss</option>";
            html = html + "</select><br><br>";
            html = html + "<input type='submit' value='Submit'>";
            html = html + "</form>";

            // Close the result set for the descriptor query
            rs.close();

            // Get user input
            String startYear = context.queryParam("startYear");
            String endYear = context.queryParam("endYear");
            String foodGroup = context.queryParam("foodGroup");
            String sortOrder = context.queryParam("sortingOrder");

            if (foodGroup != null) {
                // SQL Query to compute average loss percentages and difference
                String sql = "WITH YearlyAverages AS (" +
                             "SELECT f.cpc_code, c.DESCRIPTOR, f.year, " +
                             "AVG(f.loss_percentage) AS AvgLossPercentage " +
                             "FROM FoodLoss f " +
                             "JOIN CPC c ON SUBSTR(f.cpc_code, 1, 3) = c.\"GROUP\" " +
                             "WHERE f.year BETWEEN " + startYear + " AND " + endYear + " ";

                // Add condition for a specific food group if not "All"
                if (!foodGroup.equals("All")) {
                    sql += "AND c.DESCRIPTOR = '" + foodGroup + "' ";
                }

                sql += "GROUP BY f.cpc_code, c.DESCRIPTOR, f.year" +
                       "), ComputedAverages AS (" +
                       "SELECT DESCRIPTOR, " +
                       "AVG(CASE WHEN year = " + startYear + " THEN AvgLossPercentage ELSE NULL END) AS StartYearAverageLoss, " +
                       "AVG(CASE WHEN year = " + endYear + " THEN AvgLossPercentage ELSE NULL END) AS EndYearAverageLoss " +
                       "FROM YearlyAverages " +
                       "GROUP BY DESCRIPTOR) " +
                       "SELECT DESCRIPTOR, " +
                       "IFNULL(StartYearAverageLoss, 'No Data') AS StartYearAverageLoss, " +
                       "IFNULL(EndYearAverageLoss, 'No Data') AS EndYearAverageLoss, " +
                       "CASE " +
                       "WHEN StartYearAverageLoss != 'No Data' AND EndYearAverageLoss != 'No Data' THEN " +
                       "ROUND(((EndYearAverageLoss - StartYearAverageLoss) / StartYearAverageLoss) * 100, 2) " +
                       "ELSE 'No Data' " +
                       "END AS percentage_difference " +
                       "FROM ComputedAverages";

                if (!sortOrder.equals("None")) {
                    sql += " ORDER BY " + sortOrder;
                }

                // Execute the query and get the result set
                rs = stmt.executeQuery(sql);

                // Process the result set and generate the HTML content
                html += """
<style>
.tableCentering {
        display: flex;
        justify-content: center;
        align-items: center;
        text-align: center;
        padding-top: 2rem;
        padding-bottom: 2rem;
    }
    table {
        width: 50%;
        border-collapse: collapse;
        text-align: center;
    }
    th, td {
        border: 1px solid black;
        padding: 8px;
        text-align: left;
    }
    th {
        background-color: white;
    }
    tr:nth-child(even) {
        background-color: #f9f9f9;
    }
    tr:hover {
        background-color: #d1e7dd;
    }
</style>
""";
                html += "<div class='tableCentering'>";
                html = html + "<table border='1'>";
                html = html + "<tr><th>Food Group</th><th>" + startYear + " average percent loss</th><th>" + endYear + " average percent loss</th><th>Difference in loss %</th></tr>";
                while (rs.next()) {
                    html = html + "<tr>";
                    html = html + "<td>" + rs.getString("DESCRIPTOR") + "</td>";
                    html = html + "<td>" + rs.getString("StartYearAverageLoss") + "</td>";
                    html = html + "<td>" + rs.getString("EndYearAverageLoss") + "</td>";
                    html = html + "<td>" + rs.getString("percentage_difference") + "</td>";
                    html = html + "</tr>";
                }
                html = html + "</table>";
                html += "<div>";
            } else {
                html = html + "<p>Please enter the food group to see the results.</p>";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            html = html + "<p>Error: " + e.getMessage() + "</p>";
        } finally {
            // Close the database connections
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }

        // Close Content div
        html = html + "</div>";

        // Footer
        html = html + """
            <div class='footer'>
            <div class='bottomnav'>
            <a href="" class="bottomnav a">Terms of service</a>
            <a href="" class="bottomnav a">Privacy Policy</a>
            <a href="" class="bottomnav a">Copyright</a>
            <a href="mission.html" class="bottomnav a">Contact us</a>
           
    
        </div>
            </div>
        """;

        // Finish the HTML webpage
        html = html + "</body>" + "</html>";
        

        // DO NOT MODIFY THIS
        // Makes Javalin render the webpage
        context.html(html);
    }
}
