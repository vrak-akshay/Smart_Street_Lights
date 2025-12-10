import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class DashboardServer {
    public static void main(String[] args) throws Exception {
        int port = 9090;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new DashboardHandler());
        server.createContext("/data", new DataHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("✅ Dashboard running at http://localhost:" + port);
    }

    // Serves the HTML page
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title> Accident Control Dashboard</title>
                <style>
                    body {
                        font-family: 'Segoe UI', sans-serif;
                        background: #0f172a;
                        color: white;
                        margin: 0;
                        padding: 0;
                    }
                    header {
                        background: #1e293b;
                        padding: 20px;
                        text-align: center;
                        font-size: 24px;
                        color: #38bdf8;
                        font-weight: bold;
                        letter-spacing: 1px;
                        box-shadow: 0 2px 5px rgba(0,0,0,0.4);
                    }
                    .container {
                        padding: 20px;
                    }
                    input {
                        padding: 10px;
                        width: 300px;
                        border-radius: 5px;
                        border: none;
                        outline: none;
                        font-size: 16px;
                        margin-bottom: 15px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        background: #1e293b;
                        border-radius: 8px;
                        overflow: hidden;
                    }
                    th, td {
                        padding: 12px 15px;
                        text-align: left;
                    }
                    th {
                        background: #38bdf8;
                        color: #0f172a;
                        font-weight: bold;
                    }
                    tr:nth-child(even) {
                        background: #273449;
                    }
                    tr:hover {
                        background: #334155;
                        transition: 0.3s;
                    }
                    .new {
                        background: #16a34a !important;
                        animation: flash 1s ease-in-out;
                    }
                    @keyframes flash {
                        0% {background:#16a34a;}
                        100% {background:#273449;}
                    }
                </style>
            </head>
            <body>
                <header> Accident Monitoring Dashboard</header>
                <div class='container'>
                    <input type='text' id='search' placeholder='Search by plate, car, or location...'>
                    <table id='accidentTable'>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Plate</th>
                                <th>Car</th>
                                <th>Location</th>
                                <th>Time</th>
                            </tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>

                <script>
                    async function loadData() {
                        const res = await fetch('/data');
                        const data = await res.json();
                        const tbody = document.querySelector('#accidentTable tbody');
                        const search = document.getElementById('search').value.toLowerCase();

                        const rows = data.filter(d =>
                            d.plate.toLowerCase().includes(search) ||
                            d.car.toLowerCase().includes(search) ||
                            d.location.toLowerCase().includes(search)
                        );

                        tbody.innerHTML = '';
                        rows.forEach((r, i) => {
                            const tr = document.createElement('tr');
                            if (i === 0) tr.classList.add('new'); // highlight newest
                            tr.innerHTML = `
                                <td>${r.id}</td>
                                <td>${r.plate}</td>
                                <td>${r.car}</td>
                                <td>${r.location}</td>
                                <td>${r.time}</td>`;
                            tbody.appendChild(tr);
                        });
                    }

                    // initial load
                    loadData();
                    // refresh every 5 seconds without clearing search
                    setInterval(loadData, 5000);
                    document.getElementById('search').addEventListener('input', loadData);
                </script>
            </body>
            </html>
            """;

            byte[] response = html.getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    // Serves the JSON data for AJAX fetch
    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            List<Map<String, Object>> data = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/streetlight_db", "root", "admin")) {

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM accidents ORDER BY time DESC");

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("plate", rs.getString("plate"));
                    row.put("car", rs.getString("car"));
                    row.put("location", rs.getString("location"));
                    row.put("time", rs.getTimestamp("time").toString());
                    data.add(row);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> r = data.get(i);
                json.append(String.format(
                        "{\"id\":%d,\"plate\":\"%s\",\"car\":\"%s\",\"location\":\"%s\",\"time\":\"%s\"}",
                        r.get("id"), escape(r.get("plate")), escape(r.get("car")),
                        escape(r.get("location")), escape(r.get("time"))));
                if (i < data.size() - 1) json.append(",");
            }
            json.append("]");

            byte[] response = json.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

        private String escape(Object o) {
            return o == null ? "" : o.toString().replace("\"", "\\\"");
        }
    }
}
