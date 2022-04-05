package chat.handler.client;

import org.json.simple.JSONObject;
import chat.common.Utilities;
import chat.handler.IProtocolHandler;
import chat.model.LocalChatRoomInfo;
import chat.common.model.Protocol;

public class CreateRoomProtocolHandler extends CommonHandler implements IProtocolHandler {

    public CreateRoomProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String requestRoomId = (String) jsonMessage.get(Protocol.roomid.toString());

        boolean isRoomExisted = serverState.isRoomExistedGlobally(requestRoomId);
        boolean hasRoomAlreadyLocked = serverState.isRoomIdLocked(requestRoomId);
        boolean isRoomIdValid = Utilities.isIdValid(requestRoomId);

        if (userInfo.isRoomOwner() || hasRoomAlreadyLocked || isRoomExisted || !isRoomIdValid) {
            write(messageBuilder.createRoomResp(requestRoomId, "false"));
        } else {

            boolean canLock = peerClient.canPeersLockId(messageBuilder.lockRoom(requestRoomId));
            if (canLock) {

                peerClient.relayPeers(messageBuilder.releaseRoom(requestRoomId, "true"));
                LocalChatRoomInfo newRoom = new LocalChatRoomInfo();
                newRoom.setChatRoomId(requestRoomId);
                newRoom.setOwner(userInfo.getIdentity());
                newRoom.addMember(userInfo.getIdentity());
                serverState.getLocalChatRooms().put(requestRoomId, newRoom);

                String former = userInfo.getCurrentChatRoom();
                serverState.getLocalChatRooms().get(former).removeMember(userInfo.getIdentity());

                userInfo.setCurrentChatRoom(requestRoomId);
                userInfo.setRoomOwner(true);

                write(messageBuilder.createRoomResp(requestRoomId, "true"));
                write(messageBuilder.roomChange(former, userInfo.getCurrentChatRoom(), userInfo.getIdentity()));
                broadcastMessageToRoom(messageBuilder.roomChange(former, userInfo.getCurrentChatRoom(), userInfo.getIdentity()), former);

            } else {
                peerClient.relayPeers(messageBuilder.releaseRoom(requestRoomId, "false"));
                write(messageBuilder.createRoomResp(requestRoomId, "false"));
            }
        }
    }
}
