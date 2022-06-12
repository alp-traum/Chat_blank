package chat.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import chat.client.controller.ChatController;
import chat.client.model.ChatClientModel;
import chat.client.model.ClientNetworkConnection;
import chat.client.view.ChatFrame;

/**
 * Starts the chat-client.
 */
public class ChatClient {

  public static void main(String[] args) {

    ChatClientModel model = new ChatClientModel();
    ChatController controller = new ChatController(model);
    ChatFrame chatFrame = new ChatFrame(controller, model);

    model.addPropertyChangeListener(chatFrame);

    ClientNetworkConnection connection = new ClientNetworkConnection(model);
    model.setConnection(connection);
    connection.start();
    chatFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    chatFrame.setVisible(true);
  }
}
