package chat.handler.client;

import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.common.model.Protocol;

public class MessageProtocolHandler extends CommonHandler implements IProtocolHandler {

    public MessageProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String content = (String) jsonMessage.get(Protocol.content.toString());
        broadcastMessageToRoom(messageBuilder.message(userInfo.getIdentity(), content), userInfo.getCurrentChatRoom());
    }
}
