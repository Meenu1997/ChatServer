package chat.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.handler.management.ManagementHandler;
import chat.election.FastBullyElection;

public class FastBullyNominationMessageHandler extends ManagementHandler implements IProtocolHandler {

    public FastBullyNominationMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // accept the nomination and inform all the subordinate processes
        FastBullyElection fastBullyElectionManagementService =
                new FastBullyElection();

        // send coordinator to all the lower priority servers
        fastBullyElectionManagementService.sendCoordinatorMessage(
                serverState.getServerInfo(),
                serverState.getSubordinateServerInfoList());

        fastBullyElectionManagementService.acceptNewCoordinator(serverState.getServerInfo());

        // stop the election
        fastBullyElectionManagementService.stopElection(serverState.getServerInfo());
    }

    private static final Logger logger = LogManager.getLogger(FastBullyNominationMessageHandler.class);
}
