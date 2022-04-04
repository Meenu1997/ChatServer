package chat.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import chat.common.model.Protocol;
import chat.handler.ProtocolHandlerFactory;
import chat.model.Message;
import chat.model.UserInfo;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientConnection implements Runnable {

    private BufferedReader reader;
    private BufferedWriter writer;
    private ExecutorService pool;
    private JSONParser parser;

    private Socket clientSocket;
    private BlockingQueue<Message> messageQueue;
    private UserInfo userInfo;
    private boolean routed = false;

    private Subject currentUser;
    private ServerState serverState = ServerState.getInstance();

    public ClientConnection(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            this.messageQueue = new LinkedBlockingQueue<>();
            this.parser = new JSONParser();
            this.pool = Executors.newSingleThreadExecutor();
        } catch (Exception e) {
            logger.trace(e.getMessage());
        }
    }

    @Override
    public void run() {

        try {

            //this.currentUser = SecurityUtils.getSubject();
           // logger.trace("[INIT] " + currentUser.getPrincipal() + ", " + currentUser.isAuthenticated() + ", " + currentUser.getSession().getId());

            pool.execute(new MessageReader(reader, messageQueue));

            while (true) {

                Message msg = messageQueue.take();
                logger.trace("Processing client messages: " + msg.getMessage());

                if (!msg.isFromClient() && msg.getMessage().equalsIgnoreCase("exit")) {
                    //The client program is abruptly terminated (e.g. using Ctrl-C)
                    ProtocolHandlerFactory.newClientHandler(null, this).handle();
                    logger.trace("EOF");
                    break;
                }

                if (msg.isFromClient()) {

                    JSONObject jsonMessage = (JSONObject) parser.parse(msg.getMessage());
//                    System.out.println("Receiving: " + msg.getMessage());

                    //logger.trace("[BEFORE] " + currentUser.getPrincipal() + ", " + currentUser.isAuthenticated() + ", " + currentUser.getSession().getId());

                    ProtocolHandlerFactory.newClientHandler(jsonMessage, this).handle();

                    //logger.trace("[AFTER] " + currentUser.getPrincipal() + ", " + currentUser.isAuthenticated() + ", " + currentUser.getSession().getId());

                    String type = (String) jsonMessage.get(Protocol.type.toString());
                    if (type.equalsIgnoreCase(Protocol.quit.toString())) break;

                } else {
//                    System.out.println("Sending  : " + msg.getMessage());
                    write(msg.getMessage());
                }
            }

            pool.shutdown();
            writer.close();
            reader.close();
            clientSocket.close();

            if (userInfo != null) {
                logger.info("Client disconnected: " + userInfo.getIdentity());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            pool.shutdownNow();
        } finally {
            //performUserLogoutAndUserSessionCleanUp();

            // Let close the socket at this point as no longer use.
            // So that no side effect.
            if (!clientSocket.isClosed()) {
                try {
                    writer.close();
                    reader.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void write(String msg) {
        try {
            writer.write(msg + "\n");
            writer.flush();

            logger.trace("Message flush");

        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }

    public void performUserLogoutAndUserSessionCleanUp() {
        if (currentUser != null) {

            String sessionId = (String) currentUser.getSession().getId();
            String username = "";

            try {

                logger.info("Client disconnected: " + currentUser.getPrincipal());

                // Subject currentUser = getCurrentUser();
                username = (String) currentUser.getPrincipal();

                new PeerClient().relayPeers(JSONMessageBuilder.getInstance().notifyUserSession(username, sessionId, "logout"));

                serverState.getLocalUserSessions().remove(sessionId);

                // must be last
                currentUser.logout();

            } catch (UnknownSessionException | ExpiredSessionException ignored) {

                // handle session timeout

                System.out.println("SessionException: " + ignored.getMessage() + ". It must has expired.");

                username = serverState.getLocalUserSessions().get(sessionId).getUsername();

                new PeerClient().relayPeers(JSONMessageBuilder.getInstance().notifyUserSession(username, sessionId, "logout"));

                serverState.getLocalUserSessions().remove(sessionId);

                // session timeout consider abrupt exit
                ProtocolHandlerFactory.newClientHandler(null, this).handle();

            } finally {

                logger.info("Cleaning up " + username + " with session [" + sessionId + "].");
            }
        }
    }

    public BlockingQueue<Message> getMessageQueue() {
        return messageQueue;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isRouted() {
        return routed;
    }

    public void setRouted(boolean routed) {
        this.routed = routed;
    }

    public Subject getCurrentUser() {
        return currentUser;
    }

    private static final Logger logger = LogManager.getLogger(ClientConnection.class);
}
