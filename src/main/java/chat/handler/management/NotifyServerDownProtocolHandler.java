package chat.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.handler.IProtocolHandler;
import chat.model.Message;

public class NotifyServerDownProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public NotifyServerDownProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());

        System.out.println("Downtime notification received. Removing server --> " + serverId);

        serverState.removeServer(serverId);
        serverState.removeRemoteChatRoomsByServerId(serverId);
        serverState.removeRemoteUserSessionsByServerId(serverId);
        serverState.removeServerInCountList(serverId);
        serverState.removeServerInSuspectList(serverId);

        messageQueue.add(new Message(false, "exit"));
    }

    private static final Logger logger = LogManager.getLogger(NotifyServerDownProtocolHandler.class);
}
