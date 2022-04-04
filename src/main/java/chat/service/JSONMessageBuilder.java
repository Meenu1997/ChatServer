package chat.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import chat.common.model.Protocol;
import chat.common.model.ServerInfo;
import chat.model.ChatRoomInfo;
import chat.model.LocalChatRoomInfo;

import java.time.Instant;
import java.util.HashMap;
import java.util.stream.Collectors;

public class JSONMessageBuilder {

    private static JSONMessageBuilder instance = null;

    private JSONMessageBuilder() {
    }

    public static synchronized JSONMessageBuilder getInstance() {
        if (instance == null) instance = new JSONMessageBuilder();
        return instance;
    }

    private final ServerState serverState = ServerState.getInstance();
    private final ServerInfo serverInfo = serverState.getServerInfo();

    public String serverChange(String approved, String serverId) {
        // {"type" : "serverchange", "approved" : "true", "serverid" : "s2"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.serverchange.toString());
        jj.put(Protocol.approved.toString(), approved);
        jj.put(Protocol.serverid.toString(), serverId);
        return jj.toJSONString();
    }

    public String route(String joiningRoomId, String host, Integer port) {
        // {"type" : "route", "roomid" : "jokes", "host" : "122.134.2.4", "port" : "4445"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.route.toString());
        jj.put(Protocol.roomid.toString(), joiningRoomId);
        jj.put(Protocol.host.toString(), host);
        jj.put(Protocol.port.toString(), port.toString());
        return jj.toJSONString();
    }

    public String route(String joiningRoomId, String host, Integer port, String username, String sessionId, String password) {
        // {"type" : "route", "roomid" : "jokes", "host" : "122.134.2.4", "port" : "4445", "username" : "xxx", "sessionId" : "xxx"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.route.toString());
        jj.put(Protocol.roomid.toString(), joiningRoomId);
        jj.put(Protocol.host.toString(), host);
        jj.put(Protocol.port.toString(), port.toString());
        jj.put(Protocol.username.toString(), username);
        jj.put(Protocol.sessionid.toString(), sessionId);
        jj.put(Protocol.password.toString(), password);
        return jj.toJSONString();
    }

    public String message(String identity, String content) {
        // {"type" : "message", "identity" : "Adel", "content" : "Hi there!"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.message.toString());
        jj.put(Protocol.identity.toString(), identity);
        jj.put(Protocol.content.toString(), content);
        jj.put(Protocol.timestamp.toString(), Instant.now().toString());
        return jj.toJSONString();
    }

    public String deleteRoom(String roomId, String approved) {
        // {"type" : "deleteroom", "roomid" : "jokes", "approved" : "true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.deleteroom.toString());
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.approved.toString(), approved);
        return jj.toJSONString();
    }

    public String deleteRoomPeers(String roomId) {
        // {"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.deleteroom.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.roomid.toString(), roomId);
        return jj.toJSONString();
    }

    public String releaseRoom(String roomId, String approved) {
        // "type" : "releaseroomid", "serverid" : "s1", "roomid" : "jokes", "approved":"false"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.releaseroomid.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.approved.toString(), approved);
        return jj.toJSONString();
    }

    public String lockRoom(String roomId) {
        //{"type" : "lockroomid", "serverid" : "s1", "roomid" : "jokes"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockroomid.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.roomid.toString(), roomId);
        return jj.toJSONString();
    }

    public String createRoomResp(String roomId, String approved) {
        //{"type" : "createroom", "roomid" : "jokes", "approved" : "false"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.createroom.toString());
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.approved.toString(), approved);
        return jj.toJSONString();
    }

    public String whoByRoom(String room) {
        JSONObject jj = new JSONObject();
        //{ "type" : "roomcontents", "roomid" : "jokes", "identities" : ["Adel","Chenhao","Maria"], "owner" : "Adel" }
        jj.put(Protocol.type.toString(), Protocol.roomcontents.toString());
        jj.put(Protocol.roomid.toString(), room);
        LocalChatRoomInfo localChatRoomInfo = serverState.getLocalChatRooms().get(room);
        JSONArray ja = new JSONArray();
        ja.addAll(localChatRoomInfo.getMembers());
        jj.put(Protocol.identities.toString(), ja);
        jj.put(Protocol.owner.toString(), localChatRoomInfo.getOwner());
        return jj.toJSONString();
    }

    public String listRooms() {
        //{ "type" : "roomlist", "rooms" : ["MainHall-s1", "MainHall-s2", "jokes"] }
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.roomlist.toString());
        JSONArray ja = new JSONArray();
        for (LocalChatRoomInfo localChatRoomInfo : serverState.getLocalChatRooms().values()) {
            String chatRoomId = localChatRoomInfo.getChatRoomId();
            ja.add(chatRoomId);
        }
        ja.addAll(serverState.getRemoteChatRooms().values().stream()
                .map(ChatRoomInfo::getChatRoomId)
                .collect(Collectors.toList()));

        jj.put(Protocol.rooms.toString(), ja);

        return jj.toJSONString();
    }

    public String listServers() {
/*
        {
            "type":"serverlist", "servers": [
                                                {“serverid”:s1, “address”:”192.168.0.3”, ”port”:”4444”}
                                                {“serverid”:s2, “address”:”192.168.0.2”, ”port”:”4445”}
                                                {“serverid”:s3, “address”:”192.168.0.1”, ”port”:”4444”}
					                        ]
        }
*/

        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.serverlist.toString());

        JSONArray ja = new JSONArray();

        for (ServerInfo server : serverState.getServerInfoList()) {
            if (serverState.isOnline(server)) {
                JSONObject jo = new JSONObject();
                jo.put(Protocol.serverid.toString(), server.getServerId());
                jo.put(Protocol.address.toString(), server.getAddress());
                jo.put(Protocol.port.toString(), server.getPort());
                ja.add(jo);
            }
        }

        jj.put(Protocol.servers.toString(), ja);

        return jj.toJSONString();
    }

    public String releaseIdentity(String userId) {
        //{"type" : "releaseidentity", "serverid" : "s1", "identity" : "Adel"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.releaseidentity.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.identity.toString(), userId);
        return jj.toJSONString();
    }

    public String lockIdentity(String userId) {
        // send peer server {"type" : "lockidentity", "serverid" : "s1", "identity" : "Adel"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockidentity.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.identity.toString(), userId);
        return jj.toJSONString();
    }

    public String newIdentityResp(String approve) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.newidentity.toString());
        jj.put(Protocol.approved.toString(), approve);
        return jj.toJSONString();
    }

    public String roomChange(String former, String roomId, String identity) {
        // {"type" : "roomchange", "identity" : "Maria", "former" : "jokes", "roomid" : "jokes"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.roomchange.toString());
        jj.put(Protocol.identity.toString(), identity);
        jj.put(Protocol.former.toString(), former);
        jj.put(Protocol.roomid.toString(), roomId);
        return jj.toJSONString();
    }

    // Management Protocols

    public String lockIdentity(String serverId, String userId, String locked) {
        // {"type" : "lockidentity", "serverid" : "s2", "identity" : "Adel", "locked" : "true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockidentity.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.identity.toString(), userId);
        jj.put(Protocol.locked.toString(), locked);
        return jj.toJSONString();
    }

    public String lockRoom(String serverId, String roomId, String locked) {
        //{"type" : "lockroomid", "serverid" : "s2", "roomid" : "jokes", "locked" : "true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockroomid.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.locked.toString(), locked);
        return jj.toJSONString();
    }

    public String listRoomsClient() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.list.toString());
        return jj.toJSONString();
    }

    // Authentication Protocols

    public String authResponse(String success, String reason) {
        //{"type":"authresponse", "success":"false", "reason":"null"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authresponse.toString());
        jj.put(Protocol.success.toString(), success);
        jj.put(Protocol.reason.toString(), reason);
        return jj.toJSONString();
    }

    public String notifyUserSession(String username, String sessionId, String status) {
        // {"type" : "notifyusersession", "username" : "ray", "sessionid" : "ba64077b-85b4-40f0-a5ac-480ad3e341b3", "serverid", "s1", "status", "login"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.notifyusersession.toString());
        jj.put(Protocol.username.toString(), username);
        jj.put(Protocol.sessionid.toString(), sessionId);
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.status.toString(), status);
        return jj.toJSONString();
    }

    public String makeLoginMessage(String username, String password) {
        // {"type" : "authenticate", "username" : "ray@example.com", "password":"cheese", "rememberme":"true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authenticate.toString());
        jj.put(Protocol.username.toString(), username); // define in shiro.ini
        jj.put(Protocol.password.toString(), password); // define in shiro.ini
        jj.put(Protocol.rememberme.toString(), "false"); // true or false or not provide
        return jj.toJSONString();
    }

    // Heartbeat

    public String notifyServerDownMessage(String serverId) {
        // {"type":"notifyserverdown", "serverid":"s2"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.notifyserverdown.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        return jj.toJSONString();
    }

    public String serverUpMessage() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.serverup.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.address.toString(), serverInfo.getAddress());
        jj.put(Protocol.port.toString(), serverInfo.getPort());
        jj.put(Protocol.managementport.toString(), serverInfo.getManagementPort());
        return jj.toJSONString();
    }

    public String startElectionMessage(String serverId, String serverAddress, Long serverPort, Long
            serverManagementPort) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.startelection.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.address.toString(), serverAddress);
        jj.put(Protocol.port.toString(), String.valueOf(serverPort));
        jj.put(Protocol.managementport.toString(), String.valueOf(serverManagementPort));
        return jj.toJSONString();
    }

    public String electionAnswerMessage(String serverId, String serverAddress, Integer serverPort, Integer
            serverManagementPort) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.answerelection.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.address.toString(), serverAddress);
        jj.put(Protocol.port.toString(), String.valueOf(serverPort));
        jj.put(Protocol.managementport.toString(), String.valueOf(serverManagementPort));
        return jj.toJSONString();
    }

    public String setCoordinatorMessage(String serverId, String serverAddress, Integer serverPort, Integer
            serverManagementPort) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.coordinator.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.address.toString(), serverAddress);
        jj.put(Protocol.port.toString(), String.valueOf(serverPort));
        jj.put(Protocol.managementport.toString(), String.valueOf(serverManagementPort));
        return jj.toJSONString();
    }

    public String gossipMessage(String serverId, HashMap<String, Integer> heartbeatCountList) {
        // {"type":"gossip","serverid":"1","heartbeatcountlist":{"1":0,"2":1,"3":1,"4":2}}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.gossip.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.heartbeatcountlist.toString(), heartbeatCountList);
        return jj.toJSONString();
    }

    public String startVoteMessage(String serverId, String suspectServerId) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.startvote.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.suspectserverid.toString(), suspectServerId);
        return jj.toJSONString();
    }

    public String answerVoteMessage(String suspectServerId, String vote, String votedBy){
        // {"type":"answervote","suspectserverid":"1","vote":"YES", "votedby":"1"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.answervote.toString());
        jj.put(Protocol.suspectserverid.toString(), suspectServerId);
        jj.put(Protocol.votedby.toString(), votedBy);
        jj.put(Protocol.vote.toString(), vote);
        return jj.toJSONString();
    }

    public String iAmUpMessage(String serverId, String serverAddress, Integer serverPort, Integer
            serverManagementPort) {
        // {"type":"iamup", "serverid":"1", "address":"localhost", "port":"4444", "managementport":"5555"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.iamup.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.address.toString(), serverAddress);
        jj.put(Protocol.port.toString(), String.valueOf(serverPort));
        jj.put(Protocol.managementport.toString(), String.valueOf(serverManagementPort));
        return jj.toJSONString();
    }

    public String viewMessage(String coordinatorId, String coordinatorAddress, Integer coordinatorPort, Integer
            coordinatorManagementPort) {
        // {"type":"viewelection", "currentcoordinatorid":"1", "currentcoordinatoraddress":"localhost",
        //      "currentcoordinatorport":"4444", "currentcoordinatormanagementport":"5555"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.viewelection.toString());
        jj.put(Protocol.currentcoordinatorid.toString(), coordinatorId);
        jj.put(Protocol.currentcoordinatoraddress.toString(), coordinatorAddress);
        jj.put(Protocol.currentcoordinatorport.toString(), String.valueOf(coordinatorPort));
        jj.put(Protocol.currentcoordinatormanagementport.toString(), String.valueOf(coordinatorManagementPort));
        return jj.toJSONString();
    }

    public String nominationMessage() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.nominationelection.toString());
        return jj.toJSONString();
    }

}
