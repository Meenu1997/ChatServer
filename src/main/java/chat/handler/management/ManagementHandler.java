package chat.handler.management;

import org.json.simple.JSONObject;
import chat.model.Message;
import chat.service.JSONMessageBuilder;
import chat.service.ManagementConnection;
import chat.service.ServerState;

import javax.net.ssl.SSLSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ManagementHandler {

    protected JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    protected ServerState serverState = ServerState.getInstance();

    protected BlockingQueue<Message> messageQueue;
    private Socket clientSocket;
    protected JSONObject jsonMessage;
    private ManagementConnection managementConnection;

    public ManagementHandler(JSONObject jsonMessage, Runnable connection) {
        this.jsonMessage = jsonMessage;

        this.managementConnection = (ManagementConnection) connection;
        this.messageQueue = managementConnection.getMessageQueue();
        this.clientSocket = managementConnection.getClientSocket();
    }
}
