package socket;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// This class handles one client connection
// It runs in a separate thread (implements Runnable)
class ClientHandler implements Runnable {
    private Socket socket; // stores client socket info

    // Constructor that takes the socket from the server
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // run() method is called automatically when the thread starts
    @Override
    public void run() {
        try (
            // Get input and output streams to communicate with the client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            String line;

            // Keep reading input lines from the client
            while ((line = in.readLine()) != null) {

                // If the client sends "QUIT", close the connection
                if (line.equalsIgnoreCase("QUIT")) {
                    out.println("RESPONSE /close");
                    out.println("STATUS: 200 OK");
                    out.println("MESSAGE: Connection closed.");
                    break;
                }

                // Split message like "ADD 3 5" into parts
                String[] tokens = line.split(" ");
                if (tokens.length < 3) {
                    sendError(out, "400 BAD_REQUEST", "Invalid command format");
                    continue;
                }

                String command = tokens[0].toUpperCase();
                double result = 0.0;

                try {
                    // Convert strings to numbers
                    double num1 = Double.parseDouble(tokens[1]);
                    double num2 = Double.parseDouble(tokens[2]);

                    // Perform the correct operation based on the command
                    switch (command) {
                        case "ADD":
                            result = num1 + num2;
                            break;
                        case "SUB":
                            result = num1 - num2;
                            break;
                        case "MUL":
                            result = num1 * num2;
                            break;
                        case "DIV":
                            if (num2 == 0) throw new ArithmeticException("Divide by zero");
                            result = num1 / num2;
                            break;
                        default:
                            sendError(out, "400 UNKNOWN_COMMAND", "Unknown command");
                            continue;
                    }

                    // Send the result back to the client
                    out.println("RESPONSE /result");
                    out.println("STATUS: 200 OK");
                    out.println("RESULT: " + result);
                    out.println();

                } catch (NumberFormatException e) {
                    // Happens if the user didn't enter valid numbers
                    sendError(out, "400 BAD_ARGUMENT", "Operands must be numeric");
                } catch (ArithmeticException e) {
                    // Happens when dividing by zero
                    sendError(out, "400 DIVIDE_BY_ZERO", "Division by zero");
                } catch (Exception e) {
                    // Any other unexpected error
                    sendError(out, "500 INTERNAL_ERROR", e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            try {
                // Always close socket at the end
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    // Sends an error message back to the client
    private void sendError(PrintWriter out, String status, String message) {
        out.println("RESPONSE /error");
        out.println("STATUS: " + status);
        out.println("MESSAGE: " + message);
        out.println();
    }
}

// The main server class that listens for client connections
public class Server {
    private static final int PORT = 2025; 
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        // Thread pool lets the server handle multiple clients at once
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        System.out.println("Server started. Listening on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Keep accepting new clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Give each client to a new thread (ClientHandler)
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
