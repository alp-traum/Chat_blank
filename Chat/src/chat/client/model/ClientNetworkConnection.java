package chat.client.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Date;
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
    } catch (ConnectException ce) {
      System.out.println(ce + " no server available.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Start the network connection.
   */
  public void start() {
    running = true;
    super.start();
  }

  @Override
  public void run() {
    // TODO: change to while(running)
    while (running && socket != null) {
      System.out.println("[ClientNetworkConnection:run()] waiting for Input from Server");
      try {
        String s = reader.readLine();
        System.out.println("[ClientNetworkConnection:run()] Original JSON from server" + s);
        JSONObject input = new JSONObject(s);
        switch (input.getString("type")){
          case "login success":
            model.loggedIn();
            System.out.println("[ClientNetworkConnection:run()] Original JSON" + input);
            break;
          case "login failed":
            model.loginFailed();
            break;
          case "user joined":
            model.userJoined(input.getString("nick"));
            break;
          case "message":
            Date time = new Date();
            model.addTextMessage(input.getString("nick"), time, input.getString("content"));
            break;
          case "user left":
            model.userLeft(input.getString("nick"));
            break;
        }
      } catch (JSONException | IOException e) {
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
    try {
      JSONObject j = new JSONObject().put("type", "login").put("nick", nickname);
      // TODO: send JSON to server via socket
      writer.write(j.toString() + System.lineSeparator());
      writer.flush();
      System.out.println(j);
    } catch (JSONException | IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Send a chat message to the server.
   *
   * @param chatMessage The {@link UserTextMessage} containing the message of the user.
   */
  public void sendMessage(UserTextMessage chatMessage) {
    // TODO: insert code here
    try {
      JSONObject j = new JSONObject()
              .put("type", "post message")
              .put("content", chatMessage.getContent());
      // TODO: send JSON to server via socket
      writer.write(j.toString() + System.lineSeparator());
      writer.flush();

      model.addTextMessage(chatMessage.getSource(), chatMessage.getTime(), chatMessage.getContent());
    } catch (JSONException | IOException e) {
      e.printStackTrace();
    }
  }
}
