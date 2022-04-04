package chat.election.timeout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import chat.common.model.ServerInfo;
import chat.election.FastBullyElectionManagementService;

@DisallowConcurrentExecution
public class FastBullyAnswerMessageTimeoutFinalizer extends MessageTimeoutFinalizer {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();

        if (serverState.answerMessageReceived() || interrupted.get()) {
            // answer messages were received
            ServerInfo topCandidate = serverState.getTopCandidate();
            fastBullyElectionManagementService.sendNominationMessage(topCandidate);
            System.out.println("Answer message received. Sending nomination to : " + topCandidate.getServerId());
            fastBullyElectionManagementService
                    .startWaitingForCoordinatorMessage(serverState.getElectionCoordinatorTimeout());
            serverState.setAnswerMessageReceived(false);
        } else {
            // answer messages were not received
            // send coordinator message to lower priority servers
            fastBullyElectionManagementService.sendCoordinatorMessage(serverState.getServerInfo(),
                    serverState.getSubordinateServerInfoList());

            fastBullyElectionManagementService.acceptNewCoordinator(serverState.getServerInfo());
            fastBullyElectionManagementService.stopElection(serverState.getServerInfo());
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static final Logger logger = LogManager.getLogger(FastBullyAnswerMessageTimeoutFinalizer.class);
}
