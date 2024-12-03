package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.commands.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketHandler {

    private final Gson gson = new Gson();
    private final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    public void onConnect(Session session) {
        gameSessions.put(session, 0);
        System.out.println("New session connected: " + session.getId());
    }

    public void onClose(Session session, int statusCode, String reason) {
        gameSessions.remove(session);
        System.out.println("Session closed: " + session.getId() + " Reason: " + reason);
    }

    public void onMessage(Session session, String message) {
        try {
            if (message.contains("\"commandType\":\"JOIN_PLAYER\"")) {
                Connect command = gson.fromJson(message, Connect.class);
                gameSessions.replace(session, command.getGameID());
                handleJoinPlayer(session, command);
            } else if (message.contains("\"commandType\":\"JOIN_OBSERVER\"")) {
                Observe command = gson.fromJson(message, Observe.class);
                gameSessions.replace(session, command.getGameID());
                handleJoinObserver(session, command);
            } else if (message.contains("\"commandType\":\"MAKE_MOVE\"")) {
                MakeMove command = gson.fromJson(message, MakeMove.class);
                handleMakeMove(session, command);
            } else if (message.contains("\"commandType\":\"LEAVE\"")) {
                Leave command = gson.fromJson(message, Leave.class);
                handleLeave(session, command);
            } else if (message.contains("\"commandType\":\"RESIGN\"")) {
                Resign command = gson.fromJson(message, Resign.class);
                handleResign(session, command);
            } else {
                System.out.println("Unknown command received: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    private void handleJoinPlayer(Session session, Connect command) throws IOException {
        try {
            AuthData auth = service.UserService.getAuth(command.getAuthString());
            GameData game = service.GameService.getGameData(command.getAuthString(), command.getGameID());
            ChessGame.TeamColor joiningColor = command.getColor().toString().equalsIgnoreCase("white")
                    ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            boolean correctColor = joiningColor == ChessGame.TeamColor.WHITE
                    ? Objects.equals(game.whiteUsername(), auth.username())
                    : Objects.equals(game.blackUsername(), auth.username());

            if (!correctColor) {
                sendError(session, "Error: attempting to join with the wrong color");
                return;
            }

            sendNotification(session, "%s has joined the game as %s".formatted(auth.username(), joiningColor));
            sendGameState(session, game);
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleJoinObserver(Session session, Observe command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthString());
            GameData game = Server.gameService.getGameData(command.getAuthString(), command.getGameID());

            sendNotification(session, "%s has joined the game as an observer".formatted(auth.username()));
            sendGameState(session, game);
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleMakeMove(Session session, MakeMove command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthString());
            GameData game = Server.gameService.getGameData(command.getAuthString(), command.getGameID());
            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);

            if (userColor == null) {
                sendError(session, "Error: You are observing this game");
                return;
            }

            if (game.game().getGameOver()) {
                sendError(session, "Error: The game is already over");
                return;
            }

            if (!game.game().getTeamTurn().equals(userColor)) {
                sendError(session, "Error: It is not your turn");
                return;
            }

            game.game().makeMove(command.getMove());
            Server.gameService.updateGame(auth.authToken(), game);

            if (game.game().isInCheckmate(userColor.opponent())) {
                broadcastMessage(session, new Notification("Checkmate! %s wins!".formatted(auth.username())), true);
                game.game().setGameOver(true);
            } else if (game.game().isInCheck(userColor.opponent())) {
                broadcastMessage(session, new Notification("Check! %s has placed their opponent in check!".formatted(auth.username())), true);
            } else {
                broadcastMessage(session, new Notification("Move made by %s".formatted(auth.username())), true);
            }

            sendGameStateToAll(game);
        } catch (InvalidMoveException e) {
            sendError(session, "Error: Invalid move. " + e.getMessage());
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleLeave(Session session, Leave command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthString());
            sendNotification(session, "%s has left the game.".formatted(auth.username()));
            session.close();
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleResign(Session session, Resign command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthString());
            GameData game = Server.gameService.getGameData(command.getAuthString(), command.getGameID());
            game.game().setGameOver(true);

            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
            String opponent = userColor == ChessGame.TeamColor.WHITE ? game.blackUsername() : game.whiteUsername();

            broadcastMessage(session, new Notification("%s has resigned. %s wins!".formatted(auth.username(), opponent)), true);
            Server.gameService.updateGame(auth.authToken(), game);
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void sendNotification(Session session, String message) throws IOException {
        sendMessage(session, new Notification(message));
    }

    private void sendError(Session session, String errorMessage) throws IOException {
        sendMessage(session, new Error(errorMessage));
    }

    private void sendGameState(Session session, GameData game) throws IOException {
        sendMessage(session, new LoadGame(game.game()));
    }

    private void sendGameStateToAll(GameData game) throws IOException {
        broadcastMessageToAll(new LoadGame(game.game()));
    }

    private void sendMessage(Session session, ServerMessage message) throws IOException {
        session.getRemote().sendString(gson.toJson(message));
    }

    private void broadcastMessage(Session currSession, ServerMessage message, boolean includeSelf) throws IOException {
        for (Session session : gameSessions.keySet()) {
            if (includeSelf || !session.equals(currSession)) {
                sendMessage(session, message);
            }
        }
    }

    private void broadcastMessageToAll(ServerMessage message) throws IOException {
        for (Session session : gameSessions.keySet()) {
            sendMessage(session, message);
        }
    }

    private void handleException(Session session, Exception e) throws IOException {
        sendError(session, e.getMessage());
        System.err.println("Error: " + e.getMessage());
    }

    private ChessGame.TeamColor getTeamColor(String username, GameData game) {
        if (Objects.equals(username, game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (Objects.equals(username, game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }
}
