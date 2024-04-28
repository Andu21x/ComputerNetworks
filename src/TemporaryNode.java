// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Leonard-Andrei Dascalete
// 210025374
// Leonard.Dascalete@city.ac.uk


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends

public class TemporaryNode implements TemporaryNodeInterface {

    // Initialize privates
    private Socket socket;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            // Split address components to extract IP and port
            String[] addressComponents = startingNodeAddress.split(":");

            // Make sure the length is as expected, otherwise return false
            if (addressComponents.length != 2) {
                System.err.println("Invalid address format. Expected format: ipAddress:portNumber");
                return false;
            }

            // Create variables and assign them
            String ipAddress = addressComponents[0];
            int portNumber = Integer.parseInt(addressComponents[1]);

            // Create new socket connection for communication with other nodes.
            // Create writer and reader for I/O
            socket = new Socket(ipAddress, portNumber);
            writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // Default protocol version stated in our RFC, can be changed for future iterations
            int PROTOCOL_VERSION = 1;

            // Send the START message with the appropriate format. Add a new line at the end
            writer.write("START " + PROTOCOL_VERSION + " " + startingNodeName + "\n");
            writer.flush(); // Flush to send message

            // Initialize a variable to hold the response to our START message
            String response = reader.readLine();

            // Verify the response is as expected, otherwise return false
            if (response != null && response.startsWith("START")) {
                System.out.println("START SUCCESSFUL");
                return true;
            } else {
                System.err.println("Failed to receive valid START response.");
                System.out.println("Response: " + response);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Failed to start communication: " + e.getMessage());
            return false;
        }
    }

    public boolean store(String key, String value) {
        try {
            // Ensure each key and value ends with exactly one newline for consistent formatting
            String formattedKey = key.endsWith("\n") ? key : key + "\n";
            String formattedValue = value.endsWith("\n") ? value : value + "\n";

            // Split the formatted strings into lines to be counted
            String[] keyLines = formattedKey.split("\n", -1);
            String[] valueLines = formattedValue.split("\n", -1);

            // Calculate the correct number of lines (excluding the extra empty entry from the last newline)
            int keyLineCount = keyLines.length - 1;
            int valueLineCount = valueLines.length - 1;

            // Write the initial line indicating how many lines of keys and values follow
            writer.write("PUT? " + keyLineCount + " " + valueLineCount + "\n");

            // Write all key lines, ensuring not to add an extra newline after the last line
            for (int i = 0; i < keyLineCount; i++) {
                writer.write(keyLines[i] + "\n");
            }

            // Write all value lines, ensuring not to add an extra newline after the last line
            for (int i = 0; i < valueLineCount; i++) {
                writer.write(valueLines[i] + "\n");
            }

            writer.flush(); // Send everything we've prepared

            // Read the server's response to determine the success of the PUT operation.
            String response = reader.readLine();
            if ("SUCCESS".equals(response)) {
                System.out.println("Response: " + response);
                return true;
            } else if ("FAILED".equals(response) || response.startsWith("END")) {
                System.out.println("Response: " + response);
                closeConnection();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Store failed: " + e.getMessage());
            e.printStackTrace();
            closeConnection();
        }
        return false;
    }


    public String get(String key) {
        try {
            // Count the number of lines in the key
            String[] keyParts = key.split("\n");
            int numLines = keyParts.length - 1;  // Last split element will be an empty string if key ends with a newline

            // Send the GET? request with the number of lines
            writer.write("GET? " + numLines + "\n");
            writer.write(key + "\n");  // Send the key, including the newline
            writer.flush();

            // Confirm we are sending the right thing by visually seeing it printed
            System.out.println("Sending to server: GET? " + numLines + "\n" + key + "\n");

            // Reading the response from the server
            String response = reader.readLine();
            System.out.println("Response: " + response);

            if (response != null && response.startsWith("VALUE")) {
                // Make the number of lines into the number we receive after VALUE
                numLines = Integer.parseInt(response.split(" ")[1]);
                StringBuilder value = new StringBuilder();

                // Collect the value from subsequent lines, based on numLines number
                for (int i = 0; i < numLines; i++) {
                    if (i > 0) {
                        value.append("\n"); // Add newline BEFORE appending the next line, except before the first line
                    }
                    value.append(reader.readLine()); // Add whatever we read to the value object
                }

                return value.toString(); // Return the value object after we made it into a string
            } else if ("NOPE".equals(response)) {
                System.out.println("No value found for the key.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Get failed: " + e.getMessage());
            e.printStackTrace();
            closeConnection();
        }
        return null;
    }

    // Easy to read method for closing the connection entirely
    private void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (writer != null) writer.close();
            if (reader != null) reader.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}