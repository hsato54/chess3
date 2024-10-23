package service;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import dataaccess.*;


import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {

    //list game, join game, create game
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }



    public List<GameData> listGames(String authToken) throws UnauthorizedException {
        verifyAuthToken(authToken);
        return gameDAO.listGames();
    }


    public int createGame(String authToken, String gameName) throws UnauthorizedException, BadRequestException {
        verifyAuthToken(authToken);
        int gameID = generateUniqueGameId();
        try {
            ChessGame game = initializeChessGame();
            gameDAO.createGame(new GameData(gameID, null, null, gameName, game));
        } catch (DataAccessException e) {
            throw new BadRequestException("Error creating game: " + e.getMessage());
        }

        return gameID; // Return the newly created gameID
    }


    private AuthData verifyAuthToken(String authToken) throws UnauthorizedException {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
    }

}
