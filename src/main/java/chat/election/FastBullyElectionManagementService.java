package chat.election;

import chat.election.timeout.*;
import chat.model.Constant;
import chat.service.JSONMessageBuilder;
import chat.service.PeerClient;
import chat.service.Quartz;
import chat.service.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import chat.common.model.ServerInfo;

import java.util.List;

public class FastBullyElectionManagementService{

    protected JSONMessageBuilder jsonMessageBuilder;
    protected PeerClient peerClient;
    protected ServerState serverState;
    protected Scheduler scheduler;

    public FastBullyElectionManagementService() {
        this.jsonMessageBuilder = JSONMessageBuilder.getInstance();
        this.peerClient = new PeerClient();
        this.serverState = ServerState.getInstance();
        this.scheduler = Quartz.getInstance().getScheduler();
    }

    public void startElection(ServerInfo proposingCoordinator, List<ServerInfo> candidatesList, Long electionAnswerTimeout) {

        // System.out.println("Starting Fast-Bully Election...");

        serverState.initializeTemporaryCandidateMap();
        serverState.setAnswerMessageReceived(false);
        serverState.setOngoingElection(true);

        System.out.println("Starting election...");
        String proposingCoordinatorServerId = proposingCoordinator.getServerId();
        String proposingCoordinatorAddress = proposingCoordinator.getAddress();
        Long proposingCoordinatorPort = Long.valueOf(proposingCoordinator.getPort());
        Long proposingCoordinatorManagementPort = Long.valueOf(proposingCoordinator.getManagementPort());
        String startElectionMessage = jsonMessageBuilder
                .startElectionMessage(proposingCoordinatorServerId, proposingCoordinatorAddress,
                        proposingCoordinatorPort, proposingCoordinatorManagementPort);
        peerClient.relaySelectedPeers(candidatesList, startElectionMessage);

        startWaitingForFastBullyAnswerMessage(electionAnswerTimeout);
    }

    public void startWaitingTimer(String groupId, Long timeout, JobDetail jobDetail) {
        try {

//            System.out.println(String.format("Starting the waiting job [%s] : %s",
//                    scheduler.getSchedulerName(), jobDetail.getKey()));

            if (scheduler.checkExists(jobDetail.getKey())) {

                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

            } else {
                SimpleTrigger simpleTrigger =
                        (SimpleTrigger) TriggerBuilder.newTrigger()
                                .withIdentity(Constant.ELECTION_TRIGGER, groupId)
                                .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                                .build();

                scheduler.scheduleJob(jobDetail, simpleTrigger);
            }

        } catch (ObjectAlreadyExistsException oe) {

            // FIX this is fine, bec, since trigger is there, we can safely trigger the job, again!
            System.out.println(oe.getMessage());

            try {

                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

                //System.err.println(Arrays.toString(scheduler.getTriggerKeys(GroupMatcher.anyGroup()).toArray()));
                // [DEFAULT.MT_e8f718prrj3ol, group1.GOSSIPJOBTRIGGER, group1.CONSENSUSJOBTRIGGER, group_fast_bully.ELECTION_TRIGGER]

            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void startWaitingForFastBullyAnswerMessage(Long timeout) {
        JobDetail answerMsgTimeoutJob =
                JobBuilder.newJob(FastBullyAnswerMessageTimeoutFinalizer.class).withIdentity
                        ("answer_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, answerMsgTimeoutJob);
    }

    public void setAnswerReceivedFlag() {
        try {
            JobKey fastBullyAnswerTimeoutJobKey = new JobKey("answer_msg_timeout_job", "group_fast_bully");
            if (scheduler.checkExists(fastBullyAnswerTimeoutJobKey)) {
                scheduler.interrupt(fastBullyAnswerTimeoutJobKey);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void resetWaitingForCoordinatorMessageTimer(JobExecutionContext context, TriggerKey triggerKey, Long timeout) {
        try {
            JobDetail jobDetail = context.getJobDetail();
            if (scheduler.checkExists(jobDetail.getKey())) {

                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

            } else {

                Trigger simpleTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("election_trigger", "group_fast_bully")
                        .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                        .build();
                context.getScheduler().rescheduleJob(triggerKey, simpleTrigger);
            }

        } catch (ObjectAlreadyExistsException oe) {
            System.out.println(oe.getLocalizedMessage());

            try {

                JobDetail jobDetail = context.getJobDetail();
                System.out.println(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));

                scheduler.triggerJob(jobDetail.getKey());

            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void startWaitingForAnswerMessage(ServerInfo proposingCoordinator, Long timeout) {
        JobDetail answerMsgTimeoutJob =
                JobBuilder.newJob(ElectionAnswerMessageTimeoutFinalizer.class).withIdentity
                        ("answer_msg_timeout_job", "group_" + proposingCoordinator.getServerId()).build();
        startWaitingTimer("group_" + proposingCoordinator.getServerId(), timeout, answerMsgTimeoutJob);
    }

    public void replyAnswerForElectionMessage(ServerInfo requestingCandidate, ServerInfo me) {
        System.out.println("Replying answer for the election start message from : " + requestingCandidate.getServerId());
        String electionAnswerMessage = jsonMessageBuilder
                .electionAnswerMessage(me.getServerId(), me.getAddress(), me.getPort(), me.getManagementPort());
        peerClient.commPeerOneWay(requestingCandidate, electionAnswerMessage);
    }

    public void setupNewCoordinator(ServerInfo newCoordinator, List<ServerInfo> subordinateServerInfoList) {
        System.out.println("Informing subordinates about the new coordinator...");
        // inform subordinates about the new coordinator
        String newCoordinatorServerId = newCoordinator.getServerId();
        String newCoordinatorAddress = newCoordinator.getAddress();
        Integer newCoordinatorServerPort = newCoordinator.getPort();
        Integer newCoordinatorServerManagementPort = newCoordinator.getManagementPort();
        String setCoordinatorMessage = jsonMessageBuilder
                .setCoordinatorMessage(newCoordinatorServerId, newCoordinatorAddress, newCoordinatorServerPort,
                        newCoordinatorServerManagementPort);
        peerClient.relaySelectedPeers(subordinateServerInfoList, setCoordinatorMessage);

        // accept the new coordinator
        acceptNewCoordinator(newCoordinator);
    }

    public void acceptNewCoordinator(ServerInfo newCoordinator) {
        serverState.setCoordinator(newCoordinator);
        serverState.setOngoingElection(false);
        serverState.setViewMessageReceived(false);
        serverState.setAnswerMessageReceived(false);
        System.out.println("Accepting new coordinator --> " + newCoordinator.getServerId());
    }





    public void startWaitingForNominationOrCoordinationMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyNominationMessageTimeoutFinalizer.class).withIdentity
                        ("coordinator_or_nomination_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    public void startWaitingForCoordinatorMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyCoordinatorMessageTimeoutFinalizer.class).withIdentity
                        ("coordinator_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    /**
     * This is Boot time only election timer job.
     */
    public void startWaitingForViewMessage(Long electionAnswerTimeout) throws SchedulerException {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyViewMessageTimeoutFinalizer.class).withIdentity
                        ("view_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", electionAnswerTimeout, coordinatorMsgTimeoutJob);
    }

    public void stopElection(ServerInfo stoppingServer) {

        serverState.resetTemporaryCandidateMap();
        serverState.setOngoingElection(false);

        stopWaitingForAnswerMessage();
        stopWaitingForCoordinatorMessage();
        stopWaitingForNominationMessage();
        stopWaitingForViewMessage();
    }

    public void stopWaitingTimer(JobKey jobKey) {
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.interrupt(jobKey);
                //scheduler.deleteJob(jobKey);
//                System.out.println(String.format("Job [%s] get interrupted from [%s]",
//                        jobKey, scheduler.getSchedulerName()));
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }



    public void stopWaitingForAnswerMessage() {
        JobKey answerMsgTimeoutJobKey = new JobKey("answer_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(answerMsgTimeoutJobKey);
    }

    public void stopWaitingForNominationMessage() {
        JobKey answerMsgTimeoutJobKey = new JobKey("coordinator_or_nomination_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(answerMsgTimeoutJobKey);
    }

    public void stopWaitingForCoordinatorMessage() {
        JobKey coordinatorMsgTimeoutJobKey = new JobKey("coordinator_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(coordinatorMsgTimeoutJobKey);
    }

    public void stopWaitingForViewMessage() {
        JobKey viewMsgTimeoutJobKey = new JobKey("view_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(viewMsgTimeoutJobKey);
    }

    public void sendIamUpMessage(ServerInfo serverInfo, List<ServerInfo> serverInfoList) {
        String iAmUpMessage = jsonMessageBuilder.iAmUpMessage(serverInfo.getServerId(), serverInfo.getAddress(),
                serverInfo.getPort(), serverInfo.getManagementPort());
        peerClient.relaySelectedPeers(serverInfoList, iAmUpMessage);
    }

    public void sendViewMessage(ServerInfo sender, ServerInfo coordinator) {
        if (null == coordinator) {
            // in the beginning coordinator could be null
            coordinator = sender;
        }
        String viewMessage = jsonMessageBuilder.viewMessage(coordinator.getServerId(), coordinator.getAddress(),
                coordinator.getPort(), coordinator.getManagementPort());
        peerClient.commPeerOneWay(sender, viewMessage);
    }

    public void sendNominationMessage(ServerInfo topCandidate) {
        peerClient.commPeerOneWay(topCandidate, jsonMessageBuilder.nominationMessage());
    }

    public void sendCoordinatorMessage(ServerInfo coordinator, List<ServerInfo> subordinateServerInfoList) {
        String coordinatorMessage = jsonMessageBuilder.setCoordinatorMessage(coordinator
                .getServerId(), coordinator.getAddress(), coordinator.getPort(), coordinator.getManagementPort());
        peerClient.relaySelectedPeers(subordinateServerInfoList, coordinatorMessage);
    }

    private static final Logger logger = LogManager.getLogger(FastBullyElectionManagementService.class);
}
