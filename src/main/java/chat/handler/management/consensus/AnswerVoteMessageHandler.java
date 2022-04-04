package chat.handler.management.consensus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.handler.IProtocolHandler;
import chat.handler.management.ManagementHandler;
import chat.model.Lingo;

public class AnswerVoteMessageHandler extends ManagementHandler implements IProtocolHandler {

    public AnswerVoteMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {

        String suspectServerId = (String) jsonMessage.get(Protocol.suspectserverid.toString());
        String votedBy = (String) jsonMessage.get(Protocol.votedby.toString());
        String vote = (String) jsonMessage.get(Protocol.vote.toString());

        Lingo.Consensus C = Lingo.Consensus.valueOf(vote);
        Integer voteCount = serverState.getVoteSet().get(C);

        System.out.println(String.format("Receiving voting to kick [%s]: [%s] voted by server: [%s]",
                suspectServerId, vote, votedBy));

        if (voteCount == null) {
            serverState.getVoteSet().put(C, 1);
        } else {
            serverState.getVoteSet().put(C, voteCount + 1);
        }
    }

    private static final Logger logger = LogManager.getLogger(AnswerVoteMessageHandler.class);
}

