package chat.client.model;

import chat.client.view.chatview.UserTextMessage;

/**
 * The network-connection of the client. Establishes a connection to the server and takes
 * care of sending and receiving messages in JSON format.
 */
public class ClientNetworkConnection {

  private static final String HOST = "localhost";
  private static final int PORT = 8080;

  // TODO: insert code here

  public ClientNetworkConnection(ChatClientModel model) {
    // TODO: insert code here

  }

  /**
   * Start the network connection.
   */
  public void start() {
    // TODO: insert code here

  }


  /**
   * Stop the network-connection.
   */
  public void stop() {
    // TODO: insert code here

  }

  /**
   * Send a login-request to the server.
   *
   * @param nickname The name of the user that requests to log in.
   */
  public void sendLogin(String nickname) {
    // TODO: insert code here

  }

  /**
   * Send a chat message to the server.
   *
   * @param chatMessage The {@link UserTextMessage} containing the message of the user.
   */
  public void sendMessage(UserTextMessage chatMessage) {
    // TODO: insert code here

  }
}
