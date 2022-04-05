package chat.handler.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.model.Message;
import chat.common.model.Protocol;
import chat.model.RemoteChatRoomInfo;
import chat.common.model.ServerInfo;

public class JoinProtocolHandler extends CommonHandler implements IProtocolHandler {

    public JoinProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String joiningRoomId = (String) jsonMessage.get(Protocol.roomid.toString());
        boolean roomExistedGlobally = serverState.isRoomExistedGlobally(joiningRoomId);
        boolean isTheSameRoom = userInfo.getCurrentChatRoom().equalsIgnoreCase(joiningRoomId);
        if (userInfo.isRoomOwner() || !roomExistedGlobally || isTheSameRoom) {
            messageQueue.add(new Message(false, messageBuilder.roomChange(joiningRoomId, joiningRoomId, userInfo.getIdentity())));
        } else {

            boolean roomExistedLocally = serverState.isRoomExistedLocally(joiningRoomId);
            boolean roomExistedRemotely = serverState.isRoomExistedRemotely(joiningRoomId);

            String former = userInfo.getCurrentChatRoom();

            if (roomExistedLocally) {
                userInfo.setCurrentChatRoom(joiningRoomId);

                serverState.getLocalChatRooms().get(joiningRoomId).addMember(userInfo.getIdentity());

                broadcastMessageToRoom(messageBuilder.roomChange(former, joiningRoomId, userInfo.getIdentity()), former, userInfo.getIdentity());
                broadcastMessageToRoom(messageBuilder.roomChange(former, joiningRoomId, userInfo.getIdentity()), joiningRoomId, userInfo.getIdentity());
                messageQueue.add(new Message(false, messageBuilder.roomChange(former, joiningRoomId, userInfo.getIdentity())));
            }

            if (roomExistedRemotely) {
                RemoteChatRoomInfo remoteChatRoomInfo = serverState.getRemoteChatRooms().get(joiningRoomId);
                ServerInfo server = serverState.getServerInfoById(remoteChatRoomInfo.getManagingServer());

                messageQueue.add(new Message(false, messageBuilder.route(joiningRoomId, server.getAddress(), server.getPort())));

                clientConnection.setRouted(true);

                broadcastMessageToRoom(messageBuilder.roomChange(former, joiningRoomId, userInfo.getIdentity()), former);

                logger.info(userInfo.getIdentity() + " has routed to server " + server.getServerId());
            }

            serverState.getLocalChatRooms().get(former).removeMember(userInfo.getIdentity());
        }
    }

    private static final Logger logger = LogManager.getLogger(JoinProtocolHandler.class);
}
