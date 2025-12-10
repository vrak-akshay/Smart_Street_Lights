package mypackage;  // optional, but recommended

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class EspReceiverServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set response content type
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Read data from ESP (example: streetlightId, status, etc.)
        String streetlightId = request.getParameter("id");
        String status = request.getParameter("status");

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to MySQL
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/streetlightdb", "root", "your_password");

            // Insert data into table
            String sql = "INSERT INTO streetlight_data (streetlight_id, status) VALUES (?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, streetlightId);
            ps.setString(2, status);
            ps.executeUpdate();

            out.println("<h3>Data received successfully!</h3>");

        } catch (Exception e) {
            e.printStackTrace(out);
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
