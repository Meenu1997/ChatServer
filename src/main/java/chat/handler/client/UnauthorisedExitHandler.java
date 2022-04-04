package chat.handler.client;

import org.json.simple.JSONObject;
import chat.handler.IProtocolHandler;
import chat.model.Message;

public class UnauthorisedExitHandler extends CommonHandler implements IProtocolHandler {

    public UnauthorisedExitHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        //messageQueue.add(new Message(false, "exit")); // TODO silent exit?
        messageQueue.add(new Message(false, messageBuilder.newIdentityResp("false")));
    }
}
