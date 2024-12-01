package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import webSocketMessages.serverMessages.Error;
import webSocketMessages.serverMessages.LoadGame;
import webSocketMessages.serverMessages.Notification;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static ui.EscapeSequences.ERASE_LINE;

public class WebsocketCommunicator extends Endpoint {

    private Session session;
    private final Gson gson;

    public WebsocketCommunicator(String serverDomain) throws Exception {
        gson = new Gson();
        initializeConnection(serverDomain);
    }

    private void initializeConnection(String serverDomain) throws Exception {
        try {
            URI uri = new URI("ws://" + serverDomain + "/connect");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, uri);

            this.session.addMessageHandler((MessageHandler.Whole<String>) this::handleMessage);

        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new Exception("Failed to establish WebSocket connection.", e);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("WebSocket connection established.");
    }

    private void handleMessage(String message) {
        if (message.contains("\"serverMessageType\":\"NOTIFICATION\"")) {
            handleNotification(message);
        } else if (message.contains("\"serverMessageType\":\"ERROR\"")) {
            handleError(message);
        } else if (message.contains("\"serverMessageType\":\"LOAD_GAME\"")) {
            handleLoadGame(message);
        } else {
            System.out.println("Unknown message received: " + message);
        }
    }

    private void handleNotification(String message) {
        Notification notification = gson.fromJson(message, Notification.class);
        printNotification(notification.getMessage());
    }

    private void handleError(String message) {
        Error error = gson.fromJson(message, Error.class);
        printNotification(error.getMessage());
    }

    private void handleLoadGame(String message) {
        LoadGame loadGame = gson.fromJson(message, LoadGame.class);
        printLoadedGame(loadGame.getGame());
    }

    private void printNotification(String message) {
        System.out.print(ERASE_LINE + "\r");
        System.out.printf("\n%s\n[IN-GAME] >>> ", message);
    }

    private void printLoadedGame(ChessGame game) {
        System.out.print(ERASE_LINE + "\r\n");
        GameplayREPL.boardPrinter.updateGame(game);
        GameplayREPL.boardPrinter.printBoard(GameplayREPL.color, null);
        System.out.print("[IN-GAME] >>> ");
    }

    public void sendMessage(String message) {
        if (this.session != null && this.session.isOpen()) {
            this.session.getAsyncRemote().sendText(message);
        } else {
            System.out.println("WebSocket connection is not open. Message not sent.");
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.printf("WebSocket connection closed: %s%n", closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable thr) {
        System.err.printf("WebSocket error occurred: %s%n", thr.getMessage());
    }
}
