package chat.handler.client;

import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;

public class QuitProtocolHandler extends CommonHandler implements IProtocolHandler {

    public QuitProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String former = userInfo.getCurrentChatRoom();

        doGracefulQuit();

        if (userInfo.isRoomOwner()) {
            broadcastMessageToRoom(messageBuilder.roomChange(former, "", userInfo.getIdentity()), mainHall, userInfo.getIdentity());
        } else {
            broadcastMessageToRoom(messageBuilder.roomChange(former, "", userInfo.getIdentity()), former, userInfo.getIdentity());
        }

        write(messageBuilder.roomChange(former, "", userInfo.getIdentity()));
    }
}
