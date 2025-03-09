/**
 * A client-side class for sending and receiving messages over a network
 * using sockets.
 *
 * Key Features:
 * - Connects to a server using a socket.
 * - Sends and receives text messages.
 * - Provides methods to close the connection.
 *
 * Usage:
 * 1. Instantiate the ChatClient class with the server address and port.
 * 2. Use sendMessage to send messages.
 * 3. Use receiveMessage to read messages from the server.
 * 4. Close the connection with the close method when done.
 */


import java.io.*;
import java.net.*;

public class ChatClient {
	
    private Socket socket; // Socket used for communication with the server.
    private  BufferedReader in; //Reader for incoming messages from the server.
    private PrintWriter out; // Writer for sending messages to the server.

    

    //Constructor to Establishes a connection to the server.
    public ChatClient(String serverAddress, int port) throws IOException {
    	
    	// Create a new socket connection.
    	socket = new Socket(serverAddress, port);
    	// Initialize input reader.
    	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	// Initialize output writer.
    	out = new  PrintWriter(socket.getOutputStream(), true);
    }

    // Sends a message to the server.
    public void sendMessage(String message) {
    	
        out.println(message);
    }
    
    
    //Receives a message from the server.
    public String receiveMessage()  throws IOException {
    	
        return in.readLine(); // Read and return the message from the server.
    }

    
    // Closes the connection to the server.
    public void close() throws IOException {
        socket.close();
    }
}
