package socket;

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Simple client program that connects to the server
public class Client {
    private static String serverIP = "localhost"; // default server IP
    private static int serverPort = 1234;         // default port number

    public static void main(String[] args) {
        loadServerInfo(); // load server IP and port from a file if available

        try (
            // Create socket connection to the server
            Socket socket = new Socket(serverIP, serverPort);
            // For receiving messages from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // For sending messages to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // For reading user input from keyboard
            Scanner sc = new Scanner(System.in);
        ) {
            System.out.println("Connected to server (" + serverIP + ":" + serverPort + ")");
            System.out.println("Enter command (ADD, SUB, MUL, DIV) followed by two numbers.");
            System.out.println("Type QUIT to exit.\n");

            while (true) {
                System.out.print(">> ");
                String command = sc.nextLine().trim(); // get user input

                // If user types QUIT, send to server and exit
                if (command.equalsIgnoreCase("QUIT")) {
                    out.println("QUIT");
                    break;
                }

                // Send command to the server
                out.println(command);

                // Read response lines from the server until a blank line
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isEmpty()) break; // end of server message
                    System.out.println(line);
                }
            }

            System.out.println("Disconnected from server.");

        } catch (IOException e) {
            // Handles any network or connection errors
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    // Reads server IP and port number from "server_info.dat"
    private static void loadServerInfo() {
        File configFile = new File("server_info.dat");
        if (!configFile.exists()) {
            System.out.println("[INFO] Configuration file not found. Using default server info.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            // Read each line and parse key=value pairs
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    if (key.equalsIgnoreCase("SERVER_IP")) {
                        serverIP = value; // set IP
                    } else if (key.equalsIgnoreCase("SERVER_PORT")) {
                        serverPort = Integer.parseInt(value); // set port
                    }
                }
            }
            System.out.println("[INFO] Loaded server info from file.");
        } catch (IOException | NumberFormatException e) {
            // If something goes wrong, use default IP and port
            System.out.println("[WARNING] Failed to read config file. Using default settings.");
        }
    }
}