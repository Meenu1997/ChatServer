package chat.handler.client;

import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.model.LocalChatRoomInfo;
import chat.model.Message;
import chat.common.model.Protocol;

public class DeleteRoomProtocolHandler extends CommonHandler implements IProtocolHandler {

    public DeleteRoomProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {

        String deleteRoomId = (String) jsonMessage.get(Protocol.roomid.toString());
        boolean roomExistedLocally = serverState.isRoomExistedLocally(deleteRoomId);
        if (roomExistedLocally) {
            LocalChatRoomInfo deletingRoom = serverState.getLocalChatRooms().get(deleteRoomId);
            if (deletingRoom.getOwner().equalsIgnoreCase(userInfo.getIdentity())) {

                userInfo.setRoomOwner(false);
                userInfo.setCurrentChatRoom(mainHall);

                doDeleteRoomProtocol(deletingRoom);

                broadcastMessageToRoom(messageBuilder.roomChange(deleteRoomId, mainHall, userInfo.getIdentity()), deleteRoomId);
                broadcastMessageToRoom(messageBuilder.roomChange(deleteRoomId, mainHall, userInfo.getIdentity()), mainHall);

                messageQueue.add(new Message(false, messageBuilder.deleteRoom(deleteRoomId, "true")));
            } else {
                messageQueue.add(new Message(false, messageBuilder.deleteRoom(deleteRoomId, "false")));
            }
        } else {
            messageQueue.add(new Message(false, messageBuilder.deleteRoom(deleteRoomId, "false")));
        }
    }
}
