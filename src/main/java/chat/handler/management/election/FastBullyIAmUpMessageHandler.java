package chat.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.common.model.ServerInfo;
import chat.handler.IProtocolHandler;
import chat.handler.management.ManagementHandler;
import chat.election.FastBullyElectionManagementService;

public class FastBullyIAmUpMessageHandler extends ManagementHandler implements IProtocolHandler {

    public FastBullyIAmUpMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // reply with my view to the sender
        String senderId = (String) jsonMessage.get(Protocol.serverid.toString());
        String senderAddress = (String) jsonMessage.get(Protocol.address.toString());
        Integer senderPort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        Integer senderManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
        ServerInfo sender = new ServerInfo(senderId, senderAddress, senderPort, senderManagementPort);
        ServerInfo coordinator = serverState.getCoordinator();

        serverState.addToTemporaryCandidateMap(sender);
        new FastBullyElectionManagementService().sendViewMessage(sender, coordinator);
    }

    private static final Logger logger = LogManager.getLogger(FastBullyIAmUpMessageHandler.class);
}
