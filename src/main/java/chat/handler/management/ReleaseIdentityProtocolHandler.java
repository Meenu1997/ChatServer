package chat.handler.management;

import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.model.Message;
import chat.common.model.Protocol;

public class ReleaseIdentityProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public ReleaseIdentityProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String requestUserId = (String) jsonMessage.get(Protocol.identity.toString());
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        String lok = serverId.concat(requestUserId);
        serverState.unlockIdentity(lok);
        messageQueue.add(new Message(false, "exit"));
    }
}
