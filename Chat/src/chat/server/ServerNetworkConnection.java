package chat.server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * The network layer of the chat server. Takes care of processing both the connection requests and
 * message handling.
 */
public class ServerNetworkConnection {

  // TODO: insert code here
  ServerSocket serverSocket;
  private HashMap<String, User> userHashMap= new HashMap<>();

  /**
   * TODO: insert JavaDoc
   */
  public ServerNetworkConnection() {
    // TODO: insert code here


  }

  /**
   * Start the network-connection such that clients can establish a connection to this server.
   */
  public void start() {
    // TODO: insert code here
    try {
      Socket clientSocket = serverSocket.accept();
      OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

      JSONObject incoming = new JSONObject(in.readLine());
      switch (incoming.getString("type")) {
        case "login":
          String name = incoming.getString("nick");
          if (userHashMap.containsKey(name)) {
            out.write(new JSONObject().put("type", "login failed").toString());

          } else {
            userHashMap.put(name, new User(name, out, clientSocket));
            out.write(new JSONObject().put("type", "user joined").put("nick", name).toString());
          }
        case "post message":
          String message = incoming.getString("content");

      }


    } catch (IOException | JSONException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Stop the network-connection.
   */
  public void stop() {
    // TODO insert code here

  }
}
