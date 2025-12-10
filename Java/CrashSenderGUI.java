import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class CrashSenderGUI extends JFrame {
    private JComboBox<String> carBox;
    private JTextField plateField, locationField;
    private JTextArea logArea;
    private JButton sendBtn;

    // ✅ Update with your actual device IPs
    private static final String NODEMCU_URL = "http://10.122.97.62/data";      // NodeMCU endpoint
    private static final String ESP32_URL   = "http://10.122.97.204/location"; // ESP32 endpoint

    public CrashSenderGUI() {
        setTitle("CreateCrash");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        setSize(400, 400);
        setLocationRelativeTo(null);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel crashInfoLabel = new JLabel("Crash Info");
        crashInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        infoPanel.add(crashInfoLabel, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        infoPanel.add(new JLabel("Car Brand:"), gbc);
        gbc.gridx = 1;
        carBox = new JComboBox<>(new String[]{"Hyundai", "Maruti", "Honda", "Toyota", "Tata"});
        infoPanel.add(carBox, gbc);

        gbc.gridx = 0; gbc.gridy++;
        infoPanel.add(new JLabel("Number Plate:"), gbc);
        gbc.gridx = 1;
        plateField = new JTextField("TN22AB1234");
        infoPanel.add(plateField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        infoPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        locationField = new JTextField("Anna Salai, Chennai");
        infoPanel.add(locationField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        sendBtn = new JButton("Send Crash Report");
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        infoPanel.add(sendBtn, gbc);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        add(infoPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);

        sendBtn.addActionListener(e -> sendCrashData());
    }

    private void sendCrashData() {
        String brand = (String) carBox.getSelectedItem();
        String plate = plateField.getText().trim();
        String location = locationField.getText().trim();

        if (plate.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                // Send car data to NodeMCU
                log("Sending to NodeMCU...");
                String json = "{\"carBrand\":\"" + brand + "\",\"plate\":\"" + plate + "\"}";
                sendPost(NODEMCU_URL, json, "application/json");
                log("✔ Sent to NodeMCU: " + brand + " | " + plate);

                // Send location to ESP32
                log("Sending location to ESP32...");
                sendPost(ESP32_URL, location, "text/plain");
                log("✔ Sent to ESP32: " + location);

            } catch (Exception ex) {
                log("❌ Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    private void sendPost(String targetURL, String body, String contentType) throws Exception {
        URL url = new URL(targetURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", contentType);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        int responseCode = conn.getResponseCode();
        log("Response from " + targetURL + ": " + responseCode);
        conn.disconnect();
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CrashSenderGUI().setVisible(true));
    }
}
