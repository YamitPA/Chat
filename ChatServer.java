/**
 * A multi-client chat server application.
 *
 * Features:
 * - Allows multiple clients to connect and communicate in a chatroom.
 * - Broadcasts messages to all connected clients.
 * - Displays and updates the participant list in real-time.
 * - Server can be gracefully shut down using the "done" command.
 *
 * Usage:
 * 1. Run the server program to start the chat server.
 * 2. Clients can connect to the server using the specified port.
 * 3. Enter "done" in the server console to stop the server.
 */



import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345; // Port for the server to listen on.
    
    //Set of active clients.
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static ServerSocket serverSocket; // Server socket for accepting connections.
    private static boolean running = true; // Server running state.

    
    // Handles communication with a single client.
    static class ClientHandler implements Runnable {
    	
        private Socket socket; // Client's socket.
        private PrintWriter out; // Writer for sending messages to the client.
        private BufferedReader reader; // Reader for receiving messages  from the client.
        private String name; // Name of the connected client.

                
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        //Sends a message to this client.
        void sendMessage(String message) {
            if (out!= null) {
                out.println(message);
            }
        }

        
        //Closes the connection to the client and releases resources.
        void closeConnection() {
            try {
            	
            	// Close the input stream reader if open.
                if (reader != null)  reader.close();
                // Close the output writer if open.
                if (out != null) out.close();
                //Close the socket if it is open and not already closed.
                if (socket != null && !socket.isClosed()) socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        
        // Main logic for handling client messages and broadcasting.
        public void run() {
        	
            try (InputStream input = socket.getInputStream();
                 OutputStream output = socket.getOutputStream();
                 BufferedReader localReader = new BufferedReader(new InputStreamReader(input));
                 PrintWriter localOut = new PrintWriter(output, true)) {
                
            	// Initialize the reader and writer for this client.
                this.reader = localReader;
                this.out = localOut;

                out.println("Enter your name: ");
                name = reader.readLine(); // Read the client's name.
                System.out.println(name + " joined the chat.");
                broadcast(name + " has joined the chat.", this);
                broadcastParticipantList(); // Send the updated participant list to all clients.


                String message;
                //Loop to handle client messages.
                while ((message = reader.readLine()) != null) {
                    if (message.equals("exit")) { // Client exits the chat.
                        break;
                    }
                    
                    // Broadcast messages to all clients.
                    broadcast(name + ": " + message, this); 
                }
                
            } catch (IOException e) {
                System.out.println(name + " has disconnected.");
            } finally {
            	// Notify other clients and remove the client from the active list.
                broadcast(name + " has left the chat.", this);
                removeClient(this);
                broadcastParticipantList(); // Update and broadcast the participant list.
                closeConnection(); // Release resources associated with this client.
            }
        }
    }

    
    //Main entry point for the server application.
    public static void main(String[] args) {
        System.out.println("Chat server is running...");
        System.out.println("Type 'done' to shutdown the server");
        
        //Thread for reading console input to allow for server shutdown.
        Thread consoleThread  = new Thread(() -> {
            try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
                String input;
                while ((input = consoleReader.readLine()) != null) {
                    if (input.equalsIgnoreCase("done")) { 
                        shutdown(); // Trigger server shutdown on 'done' command.
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //Mark the thread as a daemon thread to stop when the main program exits.
        consoleThread.setDaemon(true); 
        consoleThread.start();

        try {
            // Try to create and bind the server socket with retries
            int maxRetries = 5;
            int retryCount = 0;
            boolean bound = false;

            while (!bound &&retryCount < maxRetries) {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true); // Allow reusing the port.
                    serverSocket.bind(new InetSocketAddress(PORT));
                    bound = true;
                } catch (BindException e) {
                    retryCount++;
                    if (retryCount == maxRetries) {
                        System.out.println("Failed to bind to port " + PORT + " after " + maxRetries + " attempts. Port might be in use.");
                        System.exit(1);
                    }
                    System.out.println("Port " + PORT + " is busy, waiting 2 seconds before retry " + retryCount + "/" + maxRetries);
                    Thread.sleep(2000); // Wait 2 seconds before retrying
                }
            }

            // Main server loop to accept client connections.
            while (running) {
                try {
                    Socket socket = serverSocket.accept(); // Accept new client connections.
                    System.out.println("New client connected!");
                    ClientHandler clientHandler = new ClientHandler(socket); // Create a handler for the client.
                    clients.add (clientHandler); // Add the client to the active client set.
                    new Thread(clientHandler).start(); // Start the client handler in a new thread.
                } catch (SocketException e) {
                    if (!running) {
                    	// Exit the loop if the server is shutting down.
                        System.out.println("Server stopped.");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restore the interrupted status.
            System.out.println("Server startup interrupted.");
        } finally{
            shutdown(); // Ensure the server shuts down cleanly in case of an error.
        }
    }

    
    //Shuts down the server and releases resources.
    public static void shutdown() {
        if (!running) {
            return; // Prevent multiple shutdown attempts
        }

        try {
            running = false; // Set the running flag to false to stop the server loop.
            System.out.println("Shutting down server...");

            // Close all client connections
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.closeConnection();
                }
                clients.clear(); // Clear the client set.
            }

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null; //Clear the reference
            }

            System.out.println("Server resources released.");
            
            // Give some time for the port to be released
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.exit(0); // Exit the application.
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    
    //Removes a client from the active client set.
    static void  removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
    
    // Broadcasts the participant list to all connected clients.
    static void broadcastParticipantList() {
    	
        synchronized (clients) {
        	
        	 //Build a list of participant names.
            StringBuilder participantList = new StringBuilder("PARTICIPANTS:");
            for (ClientHandler client : clients) {
                if (client.name != null) {
                    participantList.append(" ").append(client.name);
                }
            }
            // Send the participant list to all clients.
            for (ClientHandler client : clients) {
                client.sendMessage(participantList.toString());
            }
        }
    }

    
    
    //Broadcasts a message to all connected clients except the sender.
    static void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }
}