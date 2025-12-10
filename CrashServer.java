import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CrashServer {
    public static void main(String[] args) throws Exception {
        int port = 9080;
        ServerSocket server = new ServerSocket(port);
        System.out.println("✅ CrashServer started on port " + port);

        while (true) {
            Socket client = server.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            OutputStream out = client.getOutputStream();

            // Read HTTP headers
            String line;
            int contentLength = 0;
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // Read POST body (JSON)
            char[] body = new char[contentLength];
            in.read(body);
            String json = new String(body);
            System.out.println("📥 Received JSON: " + json);

            // Parse manually (simple)
            String car = extract(json, "carBrand");
            String plate = extract(json, "plate");
            String location = extract(json, "location");

            insertToDB(plate, car, location);

            // Send HTTP response
            String response = "HTTP/1.1 200 OK\r\n\r\nCrash stored in DB";
            out.write(response.getBytes());
            out.flush();
            client.close();
        }
    }

    // Simple JSON field extractor
    static String extract(String json, String key) {
        int start = json.indexOf("\"" + key + "\":\"") + key.length() + 4;
        int end = json.indexOf("\"", start);
        return (start > 3 && end > start) ? json.substring(start, end) : "";
    }

    static void insertToDB(String plate, String car, String location) {
        String url = "jdbc:mysql://localhost:3306/streetlight_db";
        String user = "root";
        String pass = "admin";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "INSERT INTO accidents (plate, car, location, time) VALUES (?, ?, ?, NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, plate);
            ps.setString(2, car);
            ps.setString(3, location);
            ps.executeUpdate();
            System.out.println("✅ Inserted crash: " + plate + " | " + car + " | " + location);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
