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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * The network layer of the chat server. Takes care of processing both the connection requests and
 * message handling.
 */
public class ServerNetworkConnection {

	// TODO: insert code here
	ServerSocket serverSocket;
	private HashMap<String, User> userHashMap = new HashMap<>();

	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

	private volatile boolean listening = false;

	int PORT = 8080;

	/**
	 * TODO: insert JavaDoc
	 */
	public ServerNetworkConnection() throws IOException {
		// TODO: insert code here


	}

	public void acceptConnections() {
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread th, Throwable ex) {
				System.out.println("Uncaught exception: " + ex);
			}
		};

		try {
			ServerSocket socket = new ServerSocket(PORT);
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
	//public void run() {
	// }

	/**
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
	 broadcast(incoming, this.user);

	 }


	 } catch (IOException | JSONException e) {
	 throw new RuntimeException(e);
	 }

	 }
	 **/

	/**
	 * Start the network-connection such that clients can establish a connection to this server.
	 */
	/**
	 public void start() throws IOException {
	 // TODO: insert code here

	 }
	 **/

	/**
	 * Method broadcasts method to all clients except the sender.
	 *
	 * @param broadcast JSONObject with message from client to be broadcasted to all other clients.
	 */
	public void broadcast(JSONObject broadcast, User originalSender) throws JSONException, IOException {
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
	/**
	 * public void stop() { // TODO insert code here
	 *
	 * }
	 **/

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
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
				OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
				while (true) {
					System.out.println("Listening for incoming messages...");
					String s = in.readLine();
					System.out.println(s);
					JSONObject incoming = new JSONObject(s);
					switch (incoming.getString("type")) {
						case "login":
							String name = incoming.getString("nick");
							if (userHashMap.containsKey(name)) {
								JSONObject nameInUse = new JSONObject().put("type", "login failed");
								System.out.println(nameInUse);
								out.write(nameInUse.toString() + System.lineSeparator());
								out.flush();
							} else {
								thisUser = new User(name, out, socket);
								userHashMap.put(name, thisUser);
								out.write(new JSONObject().put("type", "login success").toString() + System.lineSeparator());
								out.flush();
								System.out.println("Broadcasting new User to all clients");
								broadcast(new JSONObject().put("type", "user joined").put("nick", name), thisUser);
							}
							System.out.println(userHashMap);
							break;
						case "post message":
							String message = incoming.getString("content");
							// TODO: check if user is null
							if (thisUser == null) {
								System.out.println("No User logged in.");
							} else {
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
