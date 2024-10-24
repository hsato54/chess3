package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object createGame(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");

        try {
            // Parse the request body to get the game name
            CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);

            // Create the game and return the game ID
            int gameID = gameService.createGame(authToken, createGameRequest.gameName());
            resp.status(200);
            return gson.toJson(new CreateGameResponse(gameID));

        } catch (BadRequestException e) {
            resp.status(400);
            return gson.toJson(new ErrorResponse("Bad Request: " + e.getMessage()));
        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Unauthorized: " + e.getMessage()));
        }
    }

    public Object joinGame(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");

        try {
            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);

            // Join the game with the given game ID and color
            boolean success = gameService.joinGame(authToken, joinRequest.gameID(), joinRequest.playerColor());

            resp.status(200);
            return gson.toJson(new JoinGameResponse(success));

        } catch (BadRequestException e) {
            resp.status(400);
            return gson.toJson(new ErrorResponse("Bad Request: " + e.getMessage()));
        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Unauthorized: " + e.getMessage()));
        }
    }

    public Object listGames(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");

        try {
            var games = gameService.listGames(authToken);
            resp.status(200);
            return gson.toJson(games);
        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Unauthorized: " + e.getMessage()));
        }
    }

    public Object updateGame(Request req, Response resp) throws UnauthorizedException, BadRequestException {
        String authToken = req.headers("authorization");

        try {
            GameData gameData = gson.fromJson(req.body(), GameData.class);
            gameService.updateGame(authToken, gameData);
            resp.status(200);
            return "{}";
        } catch (BadRequestException e) {
            resp.status(400);
            return gson.toJson(new ErrorResponse("Bad Request: " + e.getMessage()));
        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Unauthorized: " + e.getMessage()));
        }
    }

}
