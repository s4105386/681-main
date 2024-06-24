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

public class PageMission implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/mission.html";

    @Override
    public void handle(Context context) throws Exception {
        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Head information
        html = html + "<head>" + 
               "<title>Our Mission</title>";

        // Add some CSS (external file)
        html = html + "<link rel='stylesheet' type='text/css' href='common.css' />";
        html = html + "</head>";

        // Add the body
        html = html + "<body>";

        // Add the topnav
        // This uses a Java v15+ Text Block
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
                <h1>Our Mission</h1>
            </div>
        """;

        // Add Div for page Content
        html = html + "<div class='content'>";
        html = html + """
            <h2>Our Perspective</h2>
            <p>Our website addresses the social challenge of food loss and waste by providing an accessible platform for users to explore comprehensive data and insights on food waste across various regions and commodities. By presenting this information in an unbiased and informative manner, we aim to educate and empower users to make informed decisions and advocate for effective policies to reduce food waste.</p>
        """;
        // Add HTML for the page content
        html = html + """
            <h2><br><br>Mission Statement</h2>    
            <p>An online tool that investigates the nuances of food loss throughout the whole supply chain from production to consumption. Our goal is to raise awareness and encourage positive change in the direction of sustainability by providing insight into this important issue. </p>
            """;
            html = html + "<h2><br><br>Personas</h2>";
            JDBCConnection jdbc = new JDBCConnection();
            Connection conn = null;
            try {
                // Connect to the database
                conn = DriverManager.getConnection(JDBCConnection.DATABASE);
                Statement stmt = conn.createStatement();
                String sql = "SELECT name, age, gender, location, background, needs, goals, skills_and_experience FROM Persona";
                ResultSet rs = stmt.executeQuery(sql);
    
                // Iterate through the result set and print each persona
                while (rs.next()) {
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    String gender = rs.getString("gender");
                    String location = rs.getString("location");
                    String background = rs.getString("background");
                    String needs = rs.getString("needs");
                    String goals = rs.getString("goals");
                    String skillsAndExperience = rs.getString("skills_and_experience");
    
                    html = html + "<div class='persona'>";
                    html = html + "<h3><br>Name: " + name + "</h3>";
                    html = html + "<p>Age: " + age + "</p>";
                    html = html + "<p>Gender: " + gender + "</p>";
                    html = html + "<p>Location: " + location + "</p>";
                    html = html + "<p>Background: " + background + "</p>";
                    html = html + "<p>Needs: " + needs + "</p>";
                    html = html + "<p>Goals: " + goals + "</p>";
                    html = html + "<p>Skills & Experience: " + skillsAndExperience + "</p>";
                    html = html + "</div>";
                }
    
                // Close connections
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    
            html = html + """
                <h2><br><br>Project Team</h2>
                <p>Details about you and your partners go here.</p>
                <ul>
                    <li>Shawm Renuj Nathen Andal - S4087969@student.rmit.edu.au</li>
                    <li>Anthony Duiker - s4105386@student.rmit.edu.au</li>
                    
                    <!-- Add more as needed -->
                </ul>
            """;


        // Close Content div
        html = html + "</div>";

        // Footer
        html = html + """
            <div class='footer'>
                <p>COSC2803 - Studio Project Starter Code (Apr24)</p>
            </div>
        """;

        // Finish the HTML webpage
        html = html + "</body>" + "</html>";
        

        // DO NOT MODIFY THIS
        // Makes Javalin render the webpage
        context.html(html);
    }

}
