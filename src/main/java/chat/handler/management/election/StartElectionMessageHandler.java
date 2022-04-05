package chat.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.common.model.ServerInfo;
import chat.handler.IProtocolHandler;
import chat.handler.management.ManagementHandler;

public class StartElectionMessageHandler extends ManagementHandler implements IProtocolHandler {

    public StartElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String potentialCandidateId = (String) jsonMessage.get(Protocol.serverid.toString());
        System.out.println("Received election msg from : " + potentialCandidateId);
        String myServerId = serverState.getServerInfo().getServerId();

        if (Integer.parseInt(myServerId) < Integer.parseInt(potentialCandidateId)) {
            // tell the election requester that I have a higher priority than him
            String potentialCandidateAddress = (String) jsonMessage.get(Protocol.address.toString());
            Integer potentialCandidatePort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
            Integer potentialCandidateManagementPort =
                    Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
            ServerInfo potentialCandidate =
                    new ServerInfo(potentialCandidateId, potentialCandidateAddress, potentialCandidatePort,
                            potentialCandidateManagementPort);

        }
    }

    private static final Logger logger = LogManager.getLogger(StartElectionMessageHandler.class);
}
