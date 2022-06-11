package chat.client.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import chat.client.view.chatview.UserTextMessage;

/**
 * The network-connection of the client. Establishes a connection to the server and takes
 * care of sending and receiving messages in JSON format.
 */
public class ClientNetworkConnection extends Thread {

  private static final String HOST = "localhost";
  private static final int PORT = 8080;

  Socket socket;


  ChatClientModel model;
  OutputStreamWriter writer;

  BufferedReader reader;

  private volatile boolean running = false;

  // TODO: insert code here

  public ClientNetworkConnection(ChatClientModel model) {
    // TODO: insert code here
    try {
      socket = new Socket(HOST, PORT);
      writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
      System.out.println("Writer created");
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.model = model;
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  /**
   * Start the network connection.
   */
  public void start() {
    // TODO: insert code here
    running = true;
    super.start();
  }

  @Override
  public void run() {
    while (running) {
      try {
        JSONObject input = new JSONObject(reader.readLine());
        switch (input.getString("type")){
          case "login success":
            model.loggedIn();
            break;
          case "login failed":
            model.loginFailed();
            break;


        }

        if (input.getString("type").equals("login success")) {
          model.loggedIn();
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Stop the network-connection.
   */
  /**
  public void stop() {
    // TODO: insert code here
    super.stop();
  }
  **/

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
