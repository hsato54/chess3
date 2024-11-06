package service;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import dataaccess.*;


import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {

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


    public int createGame(String authToken, String gameName) throws UnauthorizedException, BadRequestException, DataAccessException {
        verifyAuthToken(authToken);
        int gameID = generateUniqueGameId();
        try {
            ChessGame game = initializeChessGame();
            gameDAO.createGame(new GameData(gameID, null, null, gameName, game));
        } catch (DataAccessException e) {
            throw new BadRequestException("Error creating game: " + e.getMessage());
        }

        return gameID;
    }
    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException, DataAccessException {
        AuthData authData = verifyAuthToken(authToken);

        GameData gameData = fetchGameById(gameID);
        GameData updatedGameData = assignPlayerToGame(gameData, authData, color);

        if (updatedGameData == null) {
            return false;
        }

        updateGameWithPlayers(updatedGameData, gameID);
        return true;
    }


    private AuthData verifyAuthToken(String authToken) throws UnauthorizedException {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
    }
    private GameData fetchGameById(int gameID) throws BadRequestException {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new BadRequestException("Game not found: " + e.getMessage());
        }
    }

    private int generateUniqueGameId() throws DataAccessException {
        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 10000);
        } while (gameDAO.gameExists(gameID));
        return gameID;
    }

    private ChessGame initializeChessGame() {
        ChessGame game = new ChessGame();
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        game.setBoard(board);
        return game;
    }

    private GameData assignPlayerToGame(GameData gameData, AuthData authData, String color) throws BadRequestException {
        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        // Ensure that color is valid
        if (color == null || (!color.equalsIgnoreCase("WHITE") && !color.equalsIgnoreCase("BLACK"))) {
            throw new BadRequestException("Invalid color: " + color);
        }

        switch (color.toUpperCase()) {
            case "WHITE":
                if (whiteUser != null && !whiteUser.equals(authData.username())) {
                    return null;
                }
                return new GameData(gameData.gameID(), authData.username(), blackUser, gameData.gameName(), gameData.game());
            case "BLACK":
                if (blackUser != null && !blackUser.equals(authData.username())) {
                    return null;
                }
                return new GameData(gameData.gameID(), whiteUser, authData.username(), gameData.gameName(), gameData.game());
        }
        return gameData;
    }



    private void updateGameWithPlayers(GameData gameData, int gameID) throws BadRequestException, DataAccessException {
        gameDAO.updateGame(new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game()));
    }


    public void clear() {
        gameDAO.clear();
    }
}
