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

public class PageST3A implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page3A.html";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Head information
        html = html + "<head>" + 
               "<title>Subtask 3.1</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "</head>";


        // Add the topnav
        // This uses a Java v15+ Text Block
        html = html + """
            <div class='topnav'>
            <a href="" class="topnav a">Home</a>
        <a href="/mission.html" class="topnav a">About Us</a>
        <a href="/page2A.html" class="topnav a">Change By country</a>
        <a href="/page2B.html" class="topnav a">Change by food group</a>
        <a href="/page3A.html" class="topnav a">Similarity Comparison</a>
        <a href="/page3B.html" class="topnav a">Food commodoties and groups</a>
        </div>
        """;

        // Add header content block
      

        // Add Div for page Content
        html = html + "<div class='content'>";
        html = html + "<div class='Mcontent'>";
        html += "<h2>Similarity Comparisons</h2>";

        // Add HTML for the page content
       
        ArrayList<String> Years = fetchYears();
        ArrayList<String> Countries = fetchCountries();

        html = html + """
            
                <form action='/page3A.html' method='get'>
                    <label for='Year'>Year:</label>
                    <select id='Year' name='Year'>
                        <option value=''>Select Year</option>
                                """;
                                //Filling of drop menus
                                for (String year : Years) {
                                    html += "<option value='" + year + "'>" + year + "</option>";
                                }
                                html +="</select><br><br>";
                        html += """
                          <label for='Country'>Country:</label>
                <select id='Country' name='Country'>
                    <option value=''>Select Country</option>
                            """;
                            for (String country : Countries) {
                                html += "<option value='" + country + "'>" + country + "</option>";
                            }
                            html +="</select><br><br>";

                  
                    html += """

                    <label for='sorting'> Sort By:</label>
                <select id='sorting' name='sorting'>
                    <option value='None'></option>
                    <option value='FoodSimilarity'>Similar food products</option>
                    <option value='WasteSimilarity'>Similar percent of food loss/waste</option>
                    
                    
                    </select><br><br>

                    <label for='determining'> Determine with:</label>
                <select id='determining' name='determining'>
                    <option value='None'></option>
                    <option value='AbsoluteValues'>Absolute Values</option>
                    <option value='LevelOfOverlap'>Level of overlap</option>
                    
                    
                    </select><br><br>

                    <label for='number'> Number of similar countries: </label>
                <select id='number' name='number'>
                    <option value='None'></option>
                    <option value='1'>1</option>
                    <option value='2'>2</option>
                    <option value='3'>3</option>
                    <option value='4'>4</option>
                    <option value='5'>5</option>
                    
                    </select><br><br>


                    <button type='submit' class='MainButton'>Submit</button>
            </form>
            """;




            String year = context.queryParam("Year");
            String country = context.queryParam("Country");
            String SortBy = context.queryParam("sorting");
            String DetermineWith = context.queryParam("determining");
            String numSimilar = context.queryParam("number");


            JDBCConnection jdbc = new JDBCConnection();
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = DriverManager.getConnection(JDBCConnection.DATABASE);
                stmt = conn.createStatement();
                //First SQL
                if (year != null && country != null && SortBy.equals("FoodSimilarity") && DetermineWith.equals("LevelOfOverlap")) {
                    // SQL Query
                    String sql = "SELECT C.country AS Countries, " +
                    "(COUNT(DISTINCT C.commodity) - COUNT(DISTINCT CASE WHEN a_commodities.commodity IS NULL THEN C.commodity END)) AS SimilarCommodities, " +
                    "COUNT(DISTINCT C.commodity) AS TotalCommodities, " +
                    " ROUND((COUNT(DISTINCT C.commodity) - COUNT(DISTINCT CASE WHEN a_commodities.commodity IS NULL THEN C.commodity END)) * 100.0 / NULLIF(COUNT(DISTINCT C.commodity), 0), 2) AS SimilarityPercentage " +
                    " FROM FoodLoss AS C " +
                    "LEFT JOIN (" +
                    " SELECT DISTINCT commodity " +
                    " FROM FoodLoss " +
                    " WHERE country = '" + country + "' AND year = '" + year + "'" +
                    ") AS a_commodities ON C.commodity = a_commodities.commodity " +
                    "WHERE C.year = '" + year + "' AND C.country != '" + country + "'" +
                    "GROUP BY C.country " +
                    "ORDER BY SimilarityPercentage DESC " +
                    "LIMIT " + numSimilar + "";
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
                        html += "<p2> Similarity is calculated by comparing the percentage of commodities that a country has that match your country. 100% means every commodity that that country has matches your chosen countries.</p2>";
                    //Creation of table
                    html += "<div class='tableCentering'>";
                    html = html + "<table border='1'>";
                    html = html + "<tr><th>Country</th><th>Similar Commodities</th><th>Total Commodities</th><th>Similarity</th></tr>";
                    while (rs.next()) {
                        html = html + "<tr>";
                        html = html + "<td>" + rs.getString("Countries") + "</td>";
                        html = html + "<td>" + rs.getString("SimilarCommodities") + "</td>";
                        html = html + "<td>" + rs.getString("TotalCommodities") + "</td>";
                        //double SimilarCommodities = Double.parseDouble(rs.getString("SimilarCommodities"));
                        //double TotalCommodities = Double.parseDouble(rs.getString("TotalCommodities"));
                       // html = html + "<td>" + String.format("%.2f", (SimilarCommodities / TotalCommodities) * 100) + "%</td>";
                       html = html + "<td>" + rs.getString("SimilarityPercentage") + "</td>";
                        html = html + "</tr>";
                      
                                       
                    }
                    html = html + "</table>";
                    html += "<div>";
                } 
                else if (year != null && country != null && SortBy.equals("FoodSimilarity") && DetermineWith.equals("AbsoluteValues")) {
                    // SQL Query
                    String sql = "SELECT C.country AS Countries, COUNT(DISTINCT C.commodity) AS SimilarCommodities " +
                    "FROM FoodLoss AS C " +
                    "JOIN (" +
                    " SELECT DISTINCT commodity " +
                    " FROM FoodLoss " +
                    "WHERE country = 'Australia' AND year = '2013'" +
                    ") " +
                    " AS a_commodities " +
                    "ON C.commodity = a_commodities.commodity " +
                    " WHERE C.country != '" + country + "' AND year = '" + year + "'" +
                    " GROUP BY C.country " +
                    " ORDER BY SimilarCommodities desc " +
                    "LIMIT " + numSimilar + "";
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
    int countryCommodities = 0;
    Statement newStmt = conn.createStatement();
    ResultSet rsCount = newStmt.executeQuery("SELECT COUNT(DISTINCT Commodity) AS countryCommodities " +
    "FROM FoodLoss " +
    "WHERE country = 'Australia' AND year = '2013'");
                    if(rsCount.next()) {
                        countryCommodities = rsCount.getInt("countryCommodities");
                    }
                    rsCount.close();
                    newStmt.close();
    
                    html += "<p2> Similarity is calculated by comparing the number of commodities that a country has that exist in your chosen country. 100% means all of your countries commodities also exist in that country.</p2>";
                    //Creation of table
                    html += "<div class='tableCentering'>";
                    html = html + "<table border='1'>";
                    html = html + "<tr><th>Country</th><th>Similar Commodities</th><th>Similarity</th></tr>";
                    while (rs.next()) {
                        html = html + "<tr>";
                        html = html + "<td>" + rs.getString("Countries") + "</td>";
                        html = html + "<td>" + rs.getString("SimilarCommodities") + "</td>";
                        //html = html + "<td>" + rs.getString("TotalCommodities") + "</td>";
                        double SimilarCommodities = Double.parseDouble(rs.getString("SimilarCommodities"));
                        
                        
                        //double TotalCommodities = Double.parseDouble(rs.getString("TotalCommodities"));
                       html = html + "<td>" + String.format("%.2f", (SimilarCommodities / countryCommodities) * 100) + "%</td>";
                       //html = html + "<td>" + rs.getString("SimilarityPercentage") + "</td>";
                       
                        html = html + "</tr>";
                      
                                       
                    }
                    html = html + "</table>";
                    html += "<div>";
                } 















                else if (year != null && country != null && SortBy.equals("WasteSimilarity") && DetermineWith != null) {
                    // SQL Query
                    String sql = "SELECT m49CODE, country,  ROUND(SUM(CASE WHEN year = "+ year + " THEN loss_percentage ELSE NULL END), 2) AS YearTotalLoss" +
                                " FROM FoodLoss " +
                                " WHERE year = " + year + " " +
                                " GROUP BY m49CODE, country " +
                                " ORDER BY YearTotalLoss DESC";
                                //"LIMIT " + numSimilar +"";
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
    double countryPercent = 0;
    Statement newStmt = conn.createStatement();
    ResultSet rsCount = newStmt.executeQuery("SELECT ROUND(SUM(CASE WHEN country = '" + country + "' AND year = " + year + " THEN loss_percentage ELSE 0 END), 2) AS countryPercent " +
    "FROM FoodLoss " +
    "WHERE year = '" + year + "'");
                    if(rsCount.next()) {
                        countryPercent = rsCount.getDouble("countryPercent");
                    }
                    rsCount.close();
                    newStmt.close();
    
                        
                    //Creation of table

                    html += "<p2>A countries percent is worked out by adding all of the countries loss event values together. A percent over 100% indicates less similarity (Example 160%) is less similar than 90%</p2>";
                    html += "<div class='tableCentering'>";
                    html = html + "<table border='1'>";
                    html = html + "<tr><th>m49Code</th><th>Country</th><th>Year Total Loss</th><th>" + country + "'s Total Loss</th><th>Similarity Percent</th></tr>";
                    while (rs.next()) {
                        html = html + "<tr>";
                        html = html + "<td>" + rs.getString("m49CODE") + "</td>";
                        html = html + "<td>" + rs.getString("country") + "</td>";
                        html = html + "<td>" + rs.getString("YearTotalLoss") + "%</td>";
                        html = html + "<td>" + countryPercent + "%</td>";
                        double Similarity = Double.parseDouble(rs.getString("YearTotalLoss"));
                        double SimilarityCalc = (Similarity / countryPercent) * 100;
                    
                        html = html + "<td>" + String.format("%.2f",SimilarityCalc) + "%</td>";
                        //html = html + "<td>" + rs.getString("EndYearAverageLoss") + "</td>";
                        //html = html + "<td>" + rs.getDouble("percentage_difference") + "</td>";
                        html = html + "</tr>";
                      
                  
                                   
                }
                    html = html + "</table>";
                    html += "<div>";
                } 


















                else {
                    html = html + "<p>Please enter the Year, Country, Sorting option, Determining option and Number of countries to compare to.</p>";
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
