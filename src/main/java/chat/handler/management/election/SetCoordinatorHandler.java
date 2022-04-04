package chat.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.common.model.ServerInfo;
import chat.handler.IProtocolHandler;
import chat.handler.management.ManagementHandler;
import chat.service.election.BullyElectionManagementService;

public class SetCoordinatorHandler extends ManagementHandler implements IProtocolHandler {

    public SetCoordinatorHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // stop its election
        System.out.println("Received coordinator from : " + jsonMessage.get(Protocol.serverid.toString()));

        new BullyElectionManagementService().stopElection(serverState.getServerInfo());

        // accept the new coordinator
        String newCoordinatorId = (String) jsonMessage.get(Protocol.serverid.toString());
        String newCoordinatorAddress = (String) jsonMessage.get(Protocol.address.toString());
        Integer newCoordinatorPort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        Integer newCoordinatorManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
        ServerInfo newCoordinator = new ServerInfo(newCoordinatorId, newCoordinatorAddress, newCoordinatorPort,
                newCoordinatorManagementPort);

        new BullyElectionManagementService().acceptNewCoordinator(newCoordinator);
        System.out.println("Accepted new Coordinator : " + newCoordinatorId);
    }

    private static final Logger logger = LogManager.getLogger(SetCoordinatorHandler.class);
}
