package app;

import java.util.ArrayList;

import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class PageIndex implements Handler {


    public static final String URL = "/";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Header information
        html = html + "<head>" + 
               "<title>Homepage</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "</head>";

        // Add the body
        html = html + "<body>";

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

        // Add HTML for the page content
        html = html + """
            <div class="Mcontent">
        <div class="imageChange">
    <img src="CropImage.PNG" alt="crops"> 
    </div>
    <div class="Main_Header">
    <h1>Waste Insights</h1>
    </div>

    <h2>Explore the issues of food wastage and loss through info-graphics, insight and analysis all from one place!</h2>
    
    <p>This website covers a vast range of food waste statistics! </p>
      <p> Data from 1966 up until 2022! </p>
        <p> Losses of up to 65% in one year of strawberries, cauliflower and cow peas! </p>
    
    

    <button class="MainButton">
        <a href="/page2A.html">Get Started!!</a>
</div>
            """;


        // Close Content div
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


    /**
     * Get the names of the countries in the database.
     */
    public ArrayList<String> getAllCountries() {
        // Create the ArrayList of String objects to return
        ArrayList<String> countries = new ArrayList<String>();

        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = "SELECT * FROM country";
            
            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                String countryName  = results.getString("countryName");

                // Add the country object to the array
                countries.add(countryName);
            }

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just print the error
            System.err.println(e.getMessage());
            //e.printStackTrace();
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
                //e.printStackTrace();
            }
        }

        // Finally we return all of the countries
        return countries;
    }

    //HOMEPAGE IMAGE CAME FROM https://rmit.primo.exlibrisgroup.com/permalink/61RMIT_INST/4t5l5f/cdi_artstor_primary_APANOSIG_10313573846_999999; RMIT library
}


