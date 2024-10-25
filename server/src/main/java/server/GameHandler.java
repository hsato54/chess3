package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.List;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGames(Request req, Response resp) {
        String authToken = req.headers("authorization");

        try {
            List<GameData> games = gameService.listGames(authToken);

            resp.status(200);
            return gson.toJson(games);

        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Error: Unauthorized access"));
        }
    }


    public Object createGame(Request req, Response resp) {
        String authToken = req.headers("authorization");

        try {
            if (!req.body().contains("\"gameName\":")) {
                throw new BadRequestException("No gameName provided");
            }

            GameData gameData = gson.fromJson(req.body(), GameData.class);
            String gameName = gameData.gameName();

            int gameID = gameService.createGame(authToken, gameName);

            resp.status(200);
            return String.format("{ \"gameID\": %d }", gameID);

        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Error: Unauthorized access"));

        } catch (BadRequestException e) {
            resp.status(400);
            return gson.toJson(new ErrorResponse("Error: Bad request - " + e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Object joinGame(Request req, Response resp) {
        String authToken = req.headers("authorization");

        try {
            if (!req.body().contains("\"gameID\":")) {
                throw new BadRequestException("No gameID provided");
            }

            JoinGameData joinData = gson.fromJson(req.body(), JoinGameData.class);

            boolean joinSuccess = gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());

            if (!joinSuccess) {
                resp.status(403);
                return gson.toJson(new ErrorResponse("Error: Spot already taken"));
            }

            resp.status(200);
            return "{}";

        } catch (UnauthorizedException e) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Error: Unauthorized access"));

        } catch (BadRequestException e) {
            resp.status(400);
            return gson.toJson(new ErrorResponse("Error: Bad request - " + e.getMessage()));
        }
    }

    private record JoinGameData(String playerColor, int gameID) {}

    private record ErrorResponse(String message) {}
}
