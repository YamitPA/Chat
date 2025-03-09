/**
 * ChatController class handles the user interface for the chat application.
 * It manages user actions such as login, sending messages, and disconnecting from the server.
 */


import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.TextArea;



public class ChatController { 
	private ChatClient client; // ChatClient instance to manage the connection
	
	@FXML
    private TextArea displayingMessages; //TextArea for displaying chat messages
	
	@FXML
	private TextField serverName; // TextField for entering the server name.
	
    @FXML
    private TextField message; //  TextField for typing the message to be sent

    @FXML
    private ListView<String> participants; // ListView to display participants in the chat.

    
    //Disconnect from the server and close the connection.
    @FXML
    void Disconnect(ActionEvent event) {
    	try {
            if(client != null) {
            	
                client.sendMessage("exit"); // Send exit message to server
                client.close(); // Close the client connection
                client =null; // Set client to null
                Platform.runLater(() -> displayingMessages.appendText("Disconnected from server.\n"));
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //Log in to the server by providing the server address and port.
    @FXML
    void LogIn(ActionEvent event) {
    	
        try {
            String  serverAddress = serverName.getText(); // Get server address from input field
            System.out.println("Trying to connect to server: " + serverAddress);
            client = new ChatClient(serverAddress, 12345); // Create new ChatClient instance
            System.out.println("Connected to server.");

            // Start a new thread to listen for incoming messages
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = client.receiveMessage()) != null) { //Receive messages from server
                        System.out.println("Received message: " + msg);
                        String finalMsg = msg;
                        Platform.runLater(() ->  {
                            if(finalMsg.startsWith("PARTICIPANTS:")) {
                            	
                            	//Update participants list
                                participants.getItems().clear();
                                String[] parts = finalMsg.split(":");
                                if (parts.length > 1) {
                                    String[] names = parts[1].trim().split(" ");
                                    participants.getItems().addAll(names); // Add participant names to the list
                                }
                            } else {
                                displayingMessages.appendText(finalMsg + "\n"); // Display the received message
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
            Platform.runLater(() -> displayingMessages.appendText("Connected to server.\n"));
        }catch (IOException e) {
            System.err.println ("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
    //Send a message to the server.
    @FXML
    void send(ActionEvent event) {
        String  msg = message.getText(); // Get the message from the input field
        if (msg != null && !msg.isEmpty() && client != null) {
            client.sendMessage(msg); // Send the message to the server
            Platform.runLater(() -> displayingMessages.appendText ("You: " + msg + "\n"));
            message.clear(); // Clear the message input field
        }
    }

}
