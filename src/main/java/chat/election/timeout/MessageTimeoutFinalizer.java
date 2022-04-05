package chat.election.timeout;

import org.apache.logging.log4j.Logger;
import org.quartz.*;
import chat.service.ServerState;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MessageTimeoutFinalizer implements Job, InterruptableJob {

    protected ServerState serverState = ServerState.getInstance();
    protected AtomicBoolean interrupted = new AtomicBoolean(false);

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted.set(true);
//        System.out.println("Job  interrupted");
    }

    public abstract Logger getLogger();
}
