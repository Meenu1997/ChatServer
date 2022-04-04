package chat.handler.management;

import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.model.Message;
import chat.common.model.Protocol;

import java.util.Set;

public class LockRoomIdProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public LockRoomIdProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // {"type" : "lockroomid", "serverid" : "s1", "roomid" : "jokes"}
        String requestRoomId = (String) jsonMessage.get(Protocol.roomid.toString());
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());

        boolean locked = serverState.isRoomIdLocked(requestRoomId);
        Set<String> existingRooms = serverState.getLocalChatRooms().keySet();
        boolean existed = existingRooms.contains(requestRoomId);
        if (locked || existed) { // deny lock
            messageQueue.add(new Message(false, messageBuilder.lockRoom(serverId, requestRoomId, "false")));
        } else { // approve lock
            serverState.lockRoomIdentity(requestRoomId);
            messageQueue.add(new Message(false, messageBuilder.lockRoom(serverId, requestRoomId, "true")));
        }
    }
}
