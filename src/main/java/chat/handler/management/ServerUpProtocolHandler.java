package chat.handler.management;

import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.handler.IProtocolHandler;
import chat.model.Message;
import chat.common.model.ServerInfo;

public class ServerUpProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public ServerUpProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        String address = (String) jsonMessage.get(Protocol.address.toString());
        Long port = (Long) jsonMessage.get(Protocol.port.toString());
        Long managementPort = (Long) jsonMessage.get(Protocol.managementport.toString());

        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setAddress(address);
        serverInfo.setServerId(serverId);
        serverInfo.setPort(Math.toIntExact(port));
        serverInfo.setManagementPort(Math.toIntExact(managementPort));

        serverState.addServer(serverInfo);

        messageQueue.add(new Message(false, "exit"));
    }
}
