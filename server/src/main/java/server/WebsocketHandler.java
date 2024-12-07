package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import websocket.messages.*;
import websocket.commands.*;
import websocket.messages.Error;


import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebsocketHandler {

    private final Gson gson = new Gson();
    private final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        gameSessions.put(session, 0);
        System.out.println("New session connected: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        gameSessions.remove(session);
        System.out.println("Session closed: " + session + " Reason: " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand usergamecommand = gson.fromJson(message, UserGameCommand.class);
            switch (usergamecommand.getCommandType()) {
                case CONNECT -> {
                    Connect command = gson.fromJson(message, Connect.class);
                    gameSessions.replace(session, command.getGameID());
                    handleJoinPlayer(session, command);
                }
                case MAKE_MOVE -> {
                    MakeMove command = gson.fromJson(message, MakeMove.class);
                    gameSessions.replace(session, command.getGameID());
                    handleMakeMove(session, command);
                }
                case LEAVE -> {
                    Leave command = gson.fromJson(message, Leave.class);
                    handleLeave(session, command);
                }
                case RESIGN -> {
                    Resign command = gson.fromJson(message, Resign.class);
                    handleResign(session, command);
                }

            }
        }
        catch (Exception e) {
         System.err.println("Error processing message: " + e.getMessage());
        }
    }
    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error in session " + session + ": " + throwable.getMessage());
    }

    private void handleJoinPlayer(Session session, Connect command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGame(command.getGameID());
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

//    private void handleJoinObserver(Session session, Observe command) throws IOException {
//        try {
//            AuthData auth = Server.userService.getAuth(command.getAuthToken());
//            GameData game = Server.gameService.getGame(command.getGameID());
//
//            sendNotification(session, "%s has joined the game as an observer".formatted(auth.username()));
//            sendGameState(session, game);
//        } catch (Exception e) {
//            handleException(session, e);
//        }
//    }

    private void handleMakeMove(Session session, MakeMove command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGame(command.getGameID());
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
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            sendNotification(session, "%s has left the game.".formatted(auth.username()));
            session.close();
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleResign(Session session, Resign command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGame(command.getGameID());
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
        if (session == null || !session.isOpen()) {
            System.err.println("Session is closed or null, cannot send game state.");
            return;
        }

        ChessGame chessGame = game.game();
        int gameID = game.gameID();
        LoadGame loadGameMessage = new LoadGame(gameID, chessGame);

        sendMessage(session, loadGameMessage);
    }

    private void sendGameStateToAll(GameData game) throws IOException {
        ChessGame chessGame = game.game();
        int gameID = game.gameID();
        LoadGame loadGameMessage = new LoadGame(gameID, chessGame);

        for (Session session : gameSessions.keySet()) {
            if (session.isOpen()) {
                sendMessage(session, loadGameMessage);
            } else {
                System.err.println("Skipping closed session: " + session);
            }
        }
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
