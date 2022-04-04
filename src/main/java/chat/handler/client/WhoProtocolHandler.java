package chat.handler.client;

import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.model.Message;

public class WhoProtocolHandler extends CommonHandler implements IProtocolHandler {

    public WhoProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        messageQueue.add(new Message(false, messageBuilder.whoByRoom(userInfo.getCurrentChatRoom())));
    }
}
