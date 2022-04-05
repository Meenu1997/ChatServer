package chat.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.handler.IProtocolHandler;
import chat.model.Message;
import chat.model.RemoteUserSession;

public class NotifyUserSessionHandler extends ManagementHandler implements IProtocolHandler {

    public NotifyUserSessionHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String username = (String) jsonMessage.get(Protocol.username.toString());
        String sessionId = (String) jsonMessage.get(Protocol.sessionid.toString());
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        String status = (String) jsonMessage.get(Protocol.status.toString());

        if (status.equalsIgnoreCase("login")) {
            RemoteUserSession remoteUserSession = new RemoteUserSession();
            remoteUserSession.setUsername(username);
            remoteUserSession.setSessionId(sessionId);
            remoteUserSession.setManagingServerId(serverId);
            remoteUserSession.setStatus(status);

            serverState.getRemoteUserSessions().put(sessionId, remoteUserSession);

            logger.info("Added user session to the remote user session list: " + username + " [" + sessionId + "]");
        }

        else if (status.equalsIgnoreCase("logout")) {

            serverState.getRemoteUserSessions().remove(sessionId);

            logger.info("Removed user session from the remote user session list: " + username + " [" + sessionId + "]");
        }

        messageQueue.add(new Message(false, "exit"));
    }

    private static final Logger logger = LogManager.getLogger(NotifyUserSessionHandler.class);
}
