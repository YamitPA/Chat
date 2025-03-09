# Multi-Client Chat Application

## Description
This Java application implements a client-server chat system using TCP communication, allowing multiple users to join a chatroom and exchange messages. Key features include:

- **User Joining**: New users provide their name, receive the current participant list, and notify all existing users of their arrival.
- **User Leaving**: All participants are notified when a user disconnects.
- **Message Broadcasting**: Messages sent by any user are distributed to all participants.
- **Real-Time Updates**: The participant list updates dynamically as users join or leave.

The client features a JavaFX-based GUI, while the server runs as a console application.

## Project Structure
The project is divided into client and server components with the following classes:
- **Client Side**:
  - `Chat`: Main entry point, loads the JavaFX FXML interface.
  - `ChatController`: Manages the GUI, user actions (login, send, disconnect), and message handling.
  - `ChatClient`: Handles TCP socket communication with the server.
- **Server Side**:
  - `ChatServer`: Manages client connections, broadcasts messages, and updates the participant list.
  - `ClientHandler`: A thread per client to process incoming messages and maintain connections.

## Features
- **Client GUI**: Includes a text field for messages, a TextArea for chat history, a ListView for participants, and buttons for login/disconnect.
- **Server**: Runs on port 12345, accepts multiple clients, and can be shut down with the "done" command in the console.
- **TCP Communication**: Ensures reliable message delivery between server and clients.
