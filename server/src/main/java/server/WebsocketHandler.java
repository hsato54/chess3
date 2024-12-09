package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
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
    private final Map<Integer, Map<String, Session>> gameSessions = new ConcurrentHashMap<>();
    private AuthDAO authDAO;

//    @OnWebSocketConnect
//    public void onConnect(Session session) {
//        gameSessions.put(session, null);
//        System.out.println("New session connected: " + session);
//    }
//
//    @OnWebSocketClose
//    public void onClose(Session session, int statusCode, String reason) {
//        gameSessions.remove(session);
//        System.out.println("Session closed: " + session + " Reason: " + reason);
//    }

    WebsocketHandler(AuthDAO authDAO){
        this.authDAO = authDAO;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Message received: " + message);
        try {
            UserGameCommand usergamecommand = gson.fromJson(message, UserGameCommand.class);
            verifyAuthToken(usergamecommand.getAuthToken());
            switch (usergamecommand.getCommandType()) {
                case CONNECT -> {
                    Connect command = gson.fromJson(message, Connect.class);
//                    if (gameSessions.get(command.getGameID()) == null){
//                        gameSessions.put(command.getGameID(), new ConcurrentHashMap<>());
//                    }
//                    gameSessions.get(command.getGameID()).put(command.getAuthToken(), session);
                    handleConnect(session, command);
                }
                case MAKE_MOVE -> {
                    MakeMove command = gson.fromJson(message, MakeMove.class);
//                    if(gameSessions.get(command.getGameID()) == null) {
//                        gameSessions.put(command.getGameID(), new ConcurrentHashMap<>());
//                    }
//                    gameSessions.get(command.getGameID()).put(command.getAuthToken(), session);
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
         sendError(session, new Error("Invalid Authtoken"));
        }
    }
    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error in session " + session + ": " + throwable.getMessage());
    }

    private void handleConnect(Session session, Connect command) throws IOException {
        try {

            if (Server.userService == null || Server.gameService == null) {
                throw new IllegalStateException("Server services are not initialized");
            }

//            AuthData auth = Server.userService.getAuth(command.getAuthToken());
//            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());

//            if (command.getColor() == null) {
//                sendGameState(session, game);
//                return;
//            }
//
//            ChessGame.TeamColor joiningColor = command.getColor().toString().equalsIgnoreCase("white")
//                    ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
//
//            boolean correctColor = joiningColor == ChessGame.TeamColor.WHITE
//                    ? Objects.equals(game.whiteUsername(), auth.username())
//                    : Objects.equals(game.blackUsername(), auth.username());
//
//            if (!correctColor) {
//                sendError(session, "Error: attempting to join with the wrong color");
//                return;
//            }
//
//            sendNotification(session, "%s has joined the game as %s".formatted(auth.username(), joiningColor));
//            sendGameState(session, game);
//        } catch (Exception e) {
//            handleException(session, e);
//        }

            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, new Error("Error: Invalid authentication token"));
                return;
            }

            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
            if (game == null) {
                sendError(session, new Error("Error: Invalid game ID"));
                return;
            }

            if (command.getColor() != null) {
                ChessGame.TeamColor joiningColor = command.getColor().toString().equalsIgnoreCase("white")
                        ? ChessGame.TeamColor.WHITE
                        : ChessGame.TeamColor.BLACK;

                boolean correctColor = joiningColor == ChessGame.TeamColor.WHITE
                        ? Objects.equals(game.whiteUsername(), auth.username())
                        : Objects.equals(game.blackUsername(), auth.username());

                if (!correctColor) {
                    sendError(session, new Error("Error: attempting to join with the wrong color"));
                    return;
                }

                // Notify and send game state
                if (gameSessions.get(command.getGameID()) == null){
                    gameSessions.put(command.getGameID(), new ConcurrentHashMap<>());
                }
                gameSessions.get(command.getGameID()).put(command.getAuthToken(), session);
                Notification notif = new Notification("%s has joined the game as %s".formatted(auth.username(), joiningColor));
                broadcastMessage(auth.authToken(), notif, game.gameID());

            }
            else {
                // Handle observer join logic
                if (gameSessions.get(command.getGameID()) == null){
                    gameSessions.put(command.getGameID(), new ConcurrentHashMap<>());
                }
                gameSessions.get(command.getGameID()).put(command.getAuthToken(), session);
                Notification notif = new Notification("%s has joined the game as an observer".formatted(auth.username()));
                broadcastMessage(auth.authToken(), notif, game.gameID());
            }
            sendGameState(session, game);
        } catch (UnauthorizedException e) {
            sendError(session, new Error("Error: Not authorized"));
        } catch (BadRequestException e) {
            sendError(session, new Error("Error: Not a valid game"));
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleMakeMove(Session session, MakeMove command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);

            if (userColor == null) {
                sendError(session, new Error("Error: You are observing this game"));
                return;
            }

            if (game.game().getGameOver()) {
                sendError(session, new Error("Error: The game is already over"));
                return;
            }

            if (!game.game().getTeamTurn().equals(userColor)) {
                sendError(session, new Error("Error: It is not your turn"));
                return;
            }

            game.game().makeMove(command.getMove());
            Server.gameService.updateGame(auth.authToken(), game);

            if (game.game().isInCheckmate(userColor.opponent())) {
                broadcastMessage(auth.authToken(), new Notification("Checkmate! %s wins!".formatted(auth.username())), game.gameID());
                game.game().setGameOver(true);
            } else if (game.game().isInCheck(userColor.opponent())) {
                broadcastMessage(auth.authToken(), new Notification("Check! %s has placed their opponent in check!".formatted(auth.username())), game.gameID());
            } else {
                broadcastMessage(auth.authToken(), new Notification("Move made by %s".formatted(auth.username())), game.gameID());
            }

            sendGameStateToAll(game);
        } catch (InvalidMoveException e) {
            sendError(session, new Error("Error: Invalid move. " + e.getMessage()));
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleLeave(Session session, Leave command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            sendNotification(session, "%s has left the game.".formatted(auth.username()));
            gameSessions.remove(session);
            session.close();
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void handleResign(Session session, Resign command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            GameData game = Server.gameService.getGameData(command.getAuthToken(), command.getGameID());
            game.game().setGameOver(true);

            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
            String opponent = userColor == ChessGame.TeamColor.WHITE ? game.blackUsername() : game.whiteUsername();

            Notification resignNotification = new Notification("%s has resigned. %s wins!".formatted(auth.username(), opponent));

            broadcastMessage(auth.authToken(), resignNotification, game.gameID());
            sendMessage(session, resignNotification);
            Server.gameService.updateGame(auth.authToken(), game);
        } catch (Exception e) {
            handleException(session, e);
        }
    }

    private void sendNotification(Session session, String message) throws IOException {
        sendMessage(session, new Notification(message));
    }

    private void sendError(Session session, Error error) throws IOException {
        sendMessage(session, error);
    }

    private void sendGameState(Session session, GameData game) throws IOException {
        if (session == null || !session.isOpen()) {
            System.err.println("Session is closed or null, cannot send game state.");
            return;
        }
        ServerMessage message = new LoadGame(game.gameID(), game.game());
        sendMessage(session, message);
    }

    private void sendGameStateToAll(GameData game) throws IOException {
        ChessGame chessGame = game.game();
        int gameID = game.gameID();
        LoadGame loadGameMessage = new LoadGame(gameID, chessGame);

        for (Session session : gameSessions.get(gameID).values()) {
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

    private void broadcastMessage(String sender, ServerMessage message, int gameID) throws IOException {
        for (String authToken : gameSessions.get(gameID).keySet()) {
            if (!authToken.equals(sender)) {
                sendMessage(gameSessions.get(gameID).get(authToken), message);
            }
        }
    }
    private void handleException(Session session, Exception e) throws IOException {
        String errorMessage = (e.getMessage() != null) ? e.getMessage() : "An unexpected error occurred.";
        sendError(session, new Error(errorMessage));
        System.err.println("Error: " + errorMessage);
    }

    private ChessGame.TeamColor getTeamColor(String username, GameData game) {
        if (Objects.equals(username, game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (Objects.equals(username, game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    private AuthData verifyAuthToken(String authToken) throws UnauthorizedException {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
    }
}
