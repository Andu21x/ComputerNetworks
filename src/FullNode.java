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
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends

public class FullNode implements FullNodeInterface {

    // Initialize privates
    private ServerSocket serverSocket;
    private Map<String, List<String>> keyValueStore = new HashMap<>();
    private Map<String, String> networkMap = new HashMap<>();
    private final String myNodeName = "leonard.dascalete@city.ac.uk:leonode,1,test-node-1";

    public boolean listen(String ipAddress, int portNumber) {
        try {
            System.out.println("Opening the server socket on port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening on " + ipAddress + ":" + portNumber);
            return true;
        } catch (IOException e) {
            System.err.println("Could not listen on " + ipAddress + ":" + portNumber);
            e.printStackTrace();
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();  // Accept client connection
                handleClient(clientSocket);  // Handle client connection and responses
            }
        } catch (IOException e) {
            System.err.println("Server stopped accepting connections: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                OutputStreamWriter writer = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8)) {

            // Initialize response value to hold whatever it reads
            String response;
            while ((response = reader.readLine()) != null) {
                if (response.startsWith("START")) {

                    // Split the received response to parse the response components
                    String[] parts = response.split(" ");

                    // Check if the response format is correct
                    if (parts.length < 3 || !parts[0].equals("START")) {
                        writer.write("ERROR Invalid START format\n");
                        writer.flush();
                        return;
                    }

                    // Check if the protocol version is indeed '1' like in our RFC
                    if (!parts[1].equals("1")) {
                        writer.write("ERROR Unsupported protocol version: " + parts[1] + "\n");
                        writer.write("Supported protocol version: 1" + "\n");
                        writer.write("END Wrong protocol version");
                        writer.flush();
                        return;
                    }

                    // Extract the node name
                    String clientNodeName = parts[2];

                    // Log or store the client node information for easier visualisation
                    System.out.println("Received START from " + clientNodeName + " with protocol version " + parts[1]);

                    // Respond back with the correct protocol version and the node name of my server
                    writer.write("START 1 " + myNodeName + "\n");
                    writer.flush();

                } else if (response.startsWith("ECHO?")) {
                    writer.write("OHCE" + "\n");
                    writer.flush();
                    System.out.println("Responded to ECHO request with OHCE");
                } else if (response.startsWith("END")) {
                    writer.write("END ACK" + "\n");
                    writer.flush();
                    clientSocket.close();
                    return;
                } else if (response.startsWith("PUT?")) {
                    respondPut(response, reader, writer);
                } else if (response.startsWith("GET?")) {
                    respondGet(response, reader, writer);
                } else if (response.startsWith("NOTIFY?")) {
                    respondNotify(reader,writer);
                } else if (response.startsWith("NEAREST?")) {
                    respondNearest(response, writer);
                } else {
                    writer.write("END Wrong/Unknown command" + "\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void respondNotify(BufferedReader reader, OutputStreamWriter writer) throws IOException {
        try {
            // Read their information and put them in variables
            String nodeName = reader.readLine();
            String nodeAddress = reader.readLine();

            // Verify information is valid and update the network map with the new node information
            if (nodeName != null && nodeAddress != null && !nodeName.isEmpty() && !nodeAddress.isEmpty()) {
                networkMap.put(nodeName, nodeAddress);
                System.out.println("Network map updated with node: " + nodeName + " at address: " + nodeAddress);
                writer.write("NOTIFIED" + "\n");
            } else {
                writer.write("END Invalid NOTIFY format" + "\n");
            }
            writer.flush(); // Send the message after verification
        } catch (IOException e) {
            writer.write("ERROR " + e.getMessage() + "\n" + "END" + "\n");
            writer.flush();
        }
    }

    // Quarter implementation to handle wrong formats and send an END message to not hold other nodes for no reason
    private void respondNearest(String response, OutputStreamWriter writer) throws IOException {
        try {
            String[] parts = response.split(" ");
            if (parts.length != 2 || !parts[0].equals("NEAREST?")) {
                writer.write("END Invalid NEAREST? format" + "\n");
                writer.flush();
            } else {
                writer.write("END Sorry. GET not implemented yet, issue is on my side" + "\n");
            }
        } catch (Exception e) {
            writer.write("ERROR " + e.getMessage() + "\nEND\n");
            writer.flush();
        }
    }

    private void respondGet(String response, BufferedReader reader, OutputStreamWriter writer) throws IOException {
        try {
            // Split the initial GET? request to extract more precisely
            String[] parts = response.split(" ");

            // Validate the GET? format is correct
            if (parts.length != 2 || !parts[0].equals("GET?")) {
                writer.write("END Invalid GET format" + "\n");
                writer.flush();
                return;
            }

            // Parse the number of lines
            int numLines = Integer.parseInt(parts[1]);

            StringBuilder keyBuilder = new StringBuilder();

            // Read each line of the key from the client
            for (int i = 0; i < numLines; i++) {
                keyBuilder.append(reader.readLine());
                if (i < numLines - 1) {
                    keyBuilder.append("\n");
                }
            }

            // Convert the StringBuilder to a String to use as a key
            String key = keyBuilder.toString();

            // Check if the key exists in the keyValueStore list
            if (keyValueStore.containsKey(key)) {
                List<String> values = keyValueStore.get(key);
                // Send the number of lines in our VALUE response
                writer.write("VALUE " + values.size() + "\n");  // Number of values
                // Send each line of the value
                for (String value : values) {
                    writer.write(value + "\n");
                }
            } else {
                writer.write("NOPE" +"\n");
            }
            writer.flush(); // Send the final prepared message
        } catch (NumberFormatException e) {
            writer.write("END Invalid number format in GET?" + "\n");
            writer.flush();
        } catch (Exception e) {
            writer.write("ERROR " + e.getMessage() + "\n" + "END" + "\n");
            writer.flush();
        }
    }

    // Quarter implementation to handle wrong formats and send an END message to not hold other nodes for no reason
    private void respondPut(String response, BufferedReader reader, OutputStreamWriter writer) throws IOException {
        try {
            String[] parts = response.split(" ");
            if (parts.length < 3 || !parts[0].equals("PUT?") || parts[1].isEmpty() || parts[2].isEmpty()) {
                writer.write("ERROR Invalid PUT format\n");
                writer.flush();
            } else {
                writer.write("END Sorry. PUT not implemented yet, issue is on my side" + "\n");
                writer.flush();
            }
        } catch (Exception e) {
            writer.write("ERROR " + e.getMessage() + "\n" + "END" +"\n");
            writer.flush();
        }
    }
}
