package chat.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.handler.IProtocolHandler;
import chat.handler.management.ManagementHandler;

public class AnswerElectionMessageHandler extends ManagementHandler implements IProtocolHandler {

    public AnswerElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {

        System.out.println("Received answer from : " + jsonMessage.get(Protocol.serverid.toString()));


    }

    private static final Logger logger = LogManager.getLogger(AnswerElectionMessageHandler.class);
}
