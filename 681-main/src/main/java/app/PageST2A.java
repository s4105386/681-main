package app;

import java.util.ArrayList;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Example Index HTML class using Javalin
 * <p>
 * Generate a static HTML page using Javalin
 * by writing the raw HTML into a Java String object
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 * @author Halil Ali, 2024. email: halil.ali@rmit.edu.au
 */

public class PageST2A implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page2A.html";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Head information
        html = html + "<head>" + 
               "<title>Subtask 2.A</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "</head>";

        // Add the body
        //html = html + "<body>";

        // Add the topnav
        // This uses a Java v15+ Text Block
        html = html + """
            <div class='topnav'>
                <a href='/'>Home</a>
                <a href='mission.html'>About Us</a>
                <a href='page2A.html'>Change By country</a>
                <a href='page2B.html'>Change by food group</a>
                <a href='page3A.html'>Similarity Comparison</a>
                <a href='page3B.html'>Food commodoties and groups</a>
            </div>
        """;

    
        // Some CSS
        html = html + "<div class='content'>";
        html = html + "<div class='Mcontent'>";
        
        //Array lists created for the purpose of forms
        ArrayList<String> Years = fetchYears();
        ArrayList<String> Countries = fetchCountries();


        //Forms for search critera
        html = html + """
        <h3>Focused View of loss/waste change by Country</h3>
            <form action='/page2A.html' method='get'>
                <label for='startYear'>Start Year:</label>
                <select id='startYear' name='startYear'>
                    <option value=''>Select Start Year</option>
                            """;
                            //Filling of drop menus
                            for (String year : Years) {
                                html += "<option value='" + year + "'>" + year + "</option>";
                            }
                            html +="</select><br><br>";
                    html += """
          

                <label for='endYear'>Start Year:</label>
                <select id='endYear' name='endYear'>
                    <option value=''>Select End Year</option>
                            """;
                            for (String year : Years) {
                                html += "<option value='" + year + "'>" + year + "</option>";
                            }
                            html +="</select><br><br>";

                  
                    html += """


                 <label for='Country'>Country:</label>
                <select id='Country' name='country'>
                    <option value=''>Select Country</option>
                            """;
                            for (String country : Countries) {
                                html += "<option value='" + country + "'>" + country + "</option>";
                            }
                            html +="</select><br><br>";

                  
                    html += """

                <label for='sortingOrder'> Sort By:</label>
                <select id='sortingOrder' name='sortingOrder'>
                    <option value='None'></option>
                    <option value='StartYearAverageLoss'>Start year ascending loss</option>
                    <option value='EndYearAverageLoss'>End year ascending loss</option>
                    <option value='StartYearAverageLoss DESC'>Start year descending loss</option>
                    <option value='EndYearAverageLoss DESC'>End year descending loss</option>
                    </select><br><br>

                <button type='submit' class='MainButton'>Submit</button>
            </form>
                            <div class='keyE'>
            <p2>The symbol '-' is used to show a year where no relevant data has been collected.</p2>
            <p2>The average percent loss is calculated by averaging a countries waste for each commodity they have recorded. </p2>
            </div>
            
           
        """;

        
        String firstYear = context.queryParam("startYear");
        String lastYear = context.queryParam("endYear");
        String countryChosen = context.queryParam("country");
        String sortOrder = context.queryParam("sortingOrder");

        




        JDBCConnection jdbc = new JDBCConnection();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DriverManager.getConnection(JDBCConnection.DATABASE);
            stmt = conn.createStatement();
            //First SQL
            if (firstYear != null && lastYear != null && countryChosen.trim().isEmpty()) {
                // SQL Query
                String sql = "WITH ComputedAverages AS (" +
                "SELECT m49CODE, country, " +
                "ROUND(AVG(CASE WHEN year = " + firstYear + " THEN loss_percentage ELSE NULL END), 2) AS StartYearAverageLoss, " +
                "ROUND(AVG(CASE WHEN year = " + lastYear + " THEN loss_percentage ELSE NULL END), 2) AS EndYearAverageLoss " +
                "FROM FoodLoss " +
                "WHERE year IN (" + firstYear+ ", " + lastYear + ") " +
                "GROUP BY m49CODE, country) " +
                "SELECT m49CODE, country, " +
                "IFNULL(StartYearAverageLoss, '-') AS StartYearAverageLoss, " +
                "IFNULL(EndYearAverageLoss, '-') AS EndYearAverageLoss, " +
                "CASE " +
                "WHEN StartYearAverageLoss IS NOT NULL AND EndYearAverageLoss IS NOT NULL THEN " +
                "ROUND(((EndYearAverageLoss - StartYearAverageLoss) / StartYearAverageLoss) * 100, 2) " +
                "ELSE 'Insufficient Data' " +
                "END AS percentage_difference " +
                "FROM ComputedAverages";
                if (!sortOrder.equals("None")) {
                    sql += " ORDER BY " + sortOrder;
                }
                ;
                

                rs = stmt.executeQuery(sql);

                //Table formatting
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

                //Creation of table
                html += "<div class='tableCentering'>";
                html = html + "<table border='1'>";
                html = html + "<tr><th>Country's m49Code</th><th>Country</th><th>" + firstYear + " average percent loss</th><th>" + lastYear + " average percent loss</th><th>Difference in loss %</th></tr>";
                while (rs.next()) {
                    html = html + "<tr>";
                    html = html + "<td>" + rs.getString("m49CODE") + "</td>";
                    html = html + "<td>" + rs.getString("country") + "</td>";
                    html = html + "<td>" + rs.getString("StartYearAverageLoss") + "</td>";
                    html = html + "<td>" + rs.getString("EndYearAverageLoss") + "</td>";
                    html = html + "<td>" + rs.getDouble("percentage_difference") + "%</td>";
                    html = html + "</tr>";
                  
                                   
                }
                html = html + "</table>";
                html += "<div>";
            } 
            //SECOND SQL
            else if (firstYear != null && lastYear != null && countryChosen != null) {
                // SQL Query
                String sql = "WITH ComputedAverages AS (" +
                "SELECT m49CODE, country, " +
                "ROUND(AVG(CASE WHEN year = " + firstYear + " THEN loss_percentage ELSE NULL END), 2) AS StartYearAverageLoss, " +
                "ROUND(AVG(CASE WHEN year = " + lastYear + " THEN loss_percentage ELSE NULL END), 2) AS EndYearAverageLoss " +
                "FROM FoodLoss " +
                "WHERE year IN (" + firstYear+ ", " + lastYear + ") " +
                "GROUP BY m49CODE, country) " +
                "SELECT m49CODE, country, " +
                "IFNULL(StartYearAverageLoss, '-') AS StartYearAverageLoss, " +
                "IFNULL(EndYearAverageLoss, '-') AS EndYearAverageLoss, " +
                "CASE " +
                "WHEN StartYearAverageLoss IS NOT NULL AND EndYearAverageLoss IS NOT NULL THEN " +
                "ROUND(((EndYearAverageLoss - StartYearAverageLoss) / StartYearAverageLoss) * 100, 2) " +
                "ELSE 'Insufficient Data' " +
                "END AS percentage_difference " +
                "FROM ComputedAverages " +
                "WHERE country = '" + countryChosen + "'";
                if (!sortOrder.equals("None")) {
                    sql += " ORDER BY " + sortOrder;
                }
                ;
                
                

                rs = stmt.executeQuery(sql);

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
                html = html + "<tr><th>Country's m49Code</th><th>Country</th><th>" + firstYear + " average percent loss</th><th>" + lastYear + " average percent loss</th><th>Difference in loss %</th></tr>";
                while (rs.next()) {
                    html = html + "<tr>";
                    html = html + "<td>" + rs.getString("m49CODE") + "</td>";
                    html = html + "<td>" + rs.getString("country") + "</td>";
                    html = html + "<td>" + rs.getString("StartYearAverageLoss") + "</td>";
                    html = html + "<td>" + rs.getString("EndYearAverageLoss") + "</td>";
                    html = html + "<td>" + rs.getDouble("percentage_difference") + "</td>";
                    html = html + "</tr>";
                  
                                   
                }
                html = html + "</table>";
                html += "<div>";
            } 
            
            
            else {
                html = html + "<p>Please enter the start year, end year, and country to see the results.</p>";
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
        html = html + "</div>";

        // Footer
        html = html + """
            <div class='footer'>
            <div class='bottomnav'>
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

    private ArrayList<String> fetchYears() throws SQLException {
        ArrayList<String> years = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBCConnection.DATABASE);
            stmt = conn.createStatement();
            String sql = "SELECT DISTINCT year FROM FoodLoss ORDER BY year ASC";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                years.add(rs.getString("year"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }

        return years;
    }
    private ArrayList<String> fetchCountries() throws SQLException {
        ArrayList<String> countries = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(JDBCConnection.DATABASE);
            stmt = conn.createStatement();
            String sql = "SELECT DISTINCT country FROM FoodLoss ORDER BY country ASC";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                countries.add(rs.getString("country"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }

        return countries;
    }

}
