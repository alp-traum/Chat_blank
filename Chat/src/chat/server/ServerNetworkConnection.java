package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The network layer of the chat server. Takes care of processing both the connection requests and
 * message handling.
 */
public class ServerNetworkConnection {

  ServerSocket serverSocket;
  private HashMap<String, User> userHashMap = new HashMap<>();

  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

  private volatile boolean listening = false;

  int port = 8080;

  /**
   * Constructor of server network connection. Initializes and does nothing more.
   */
  public ServerNetworkConnection() {}

  /**
  * Thread waiting for new client connections.
  */
  public void acceptConnections() {
    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread th, Throwable ex) {
        System.out.println("Uncaught exception: " + ex);
      }
    };

    try {
      ServerSocket socket = new ServerSocket(port);
      while (true) {
        System.out.println("Listening for connections....");
        Socket newConn = socket.accept();
        Thread receivingThread = new Thread(new ClientHandler(newConn));
        receivingThread.setUncaughtExceptionHandler(h);
        receivingThread.start();
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void start() {
    listening = true;
  }

  /**
   * Method broadcasts method to all clients except the sender.
   *
   * @param broadcast JSONObject with message from client to be broadcast to all other clients.
   * @param originalSender sender of the message. Should not get his own message.
   * @throws JSONException when wrong JSON as input.
   * @throws IOException wrong input.
   */
  public void broadcast(JSONObject broadcast, User originalSender)
          throws JSONException, IOException {
    Date date = new Date();
    String time = sdf.format(new Timestamp(date.getTime()));
    String msg;
    switch (broadcast.getString("type")) {
      case "user joined":
      case "user left":
        msg = broadcast.toString();
        break;
      case "post message":
        msg = new JSONObject()
                .put("type", "message")
                .put("time", time)
                .put("nick", originalSender.getName())
                .put("content", broadcast.get("content")).toString();
        break;
      default:
        msg = new JSONObject()
                .put("type", "error").toString();
        System.out.println("Unexpected message type.");
    }

    System.out.println(userHashMap);
    for (User user : userHashMap.values()) {
      if (user != originalSender) {
        OutputStreamWriter userOsw = user.getWriter();
        userOsw.write(msg + System.lineSeparator());
        userOsw.flush();
      }
    }
  }

  /**
   * Stop the network-connection.
   */
  public void stop() {
    listening = false;
  }


  class ClientHandler implements Runnable {
    Socket socket;

    public ClientHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      receiveJson();
    }

    private void receiveJson() {
      User thisUser = null;
      listening = true;
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                StandardCharsets.UTF_8));
        OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8);
        while (listening) {
          System.out.println("Listening for incoming messages...");
          String s = in.readLine();
          System.out.println(s);
          JSONObject incoming = new JSONObject(s);
          switch (incoming.getString("type")) {
            case "login":
              String name = incoming.getString("nick");
              if (userHashMap.containsKey(name) || name.equals("")) {
                JSONObject nameInUse = new JSONObject().put("type", "login failed");
                System.out.println(nameInUse);
                out.write(nameInUse.toString() + System.lineSeparator());
                out.flush();
              } else {
                thisUser = new User(name, out, socket);
                userHashMap.put(name, thisUser);
                out.write(new JSONObject().put("type", "login success").toString()
                        + System.lineSeparator());
                out.flush();
                System.out.println("Broadcasting new User to all clients");
                broadcast(new JSONObject().put("type", "user joined").put("nick", name), thisUser);
              }
              System.out.println(userHashMap);
              break;
            case "post message":
              if (thisUser == null) {
                System.out.println("No User logged in.");
              } else {
                System.out.println("Post message: " + incoming);
                broadcast(incoming, thisUser);
              }
              break;
            default:
              System.out.println("No accepted Type in JSON");
          }
        }
      } catch (IOException | JSONException e) {
        System.out.println(e.getClass());

        if (e.getClass() == java.net.SocketException.class) {
          if (thisUser != null) {
            userHashMap.remove(thisUser.getName());
            try {
              broadcast(new JSONObject()
                      .put("type", "user left")
                      .put("nick", thisUser.getName()), thisUser);
            } catch (JSONException | IOException ex) {
              ex.printStackTrace();
            }
            System.out.println("Client terminated connection " + thisUser.getName());
          } else {
            System.out.println("Unannounced user disconnected");
          }

        } else {
          throw new RuntimeException(e);
        }
      }

    }

  }

}
