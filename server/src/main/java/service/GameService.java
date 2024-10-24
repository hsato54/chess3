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
    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException {
        AuthData authData = verifyAuthToken(authToken); // Verify the authToken

        // Retrieve the game data using the gameID
        GameData gameData = fetchGameById(gameID);

        // Assign the player to either the "WHITE" or "BLACK" team
        if (!assignPlayerToGame(gameData, authData, color)) {
            return false; // Joining failed (spot already taken or invalid color)
        }

        // Update the game with the new player information
        updateGameWithPlayers(gameData, gameID);
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

    // Generate a unique game ID
    private int generateUniqueGameId() throws DataAccessException {
        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 10000);
        } while (gameDAO.gameExists(gameID));
        return gameID;
    }

    // Initialize a new ChessGame and ChessBoard
    private ChessGame initializeChessGame() {
        ChessGame game = new ChessGame();
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        game.setBoard(board);
        return game;
    }

    // Assign a player to the game (either "WHITE" or "BLACK")
    private boolean assignPlayerToGame(GameData gameData, AuthData authData, String color) throws BadRequestException {
        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        // Ensure that color is valid
        if (color == null || (!color.equalsIgnoreCase("WHITE") && !color.equalsIgnoreCase("BLACK"))) {
            throw new BadRequestException("Invalid color: " + color);
        }

        switch (color.toUpperCase()) {
            case "WHITE":
                if (whiteUser != null && !whiteUser.equals(authData.username())) {
                    return false; // White spot is already taken
                }
                // Create a new GameData object with the updated whiteUsername
                gameData = new GameData(gameData.gameID(), authData.username(), blackUser, gameData.gameName(), gameData.game());
                break;
            case "BLACK":
                if (blackUser != null && !blackUser.equals(authData.username())) {
                    return false; // Black spot is already taken
                }
                // Create a new GameData object with the updated blackUsername
                gameData = new GameData(gameData.gameID(), whiteUser, authData.username(), gameData.gameName(), gameData.game());
                break;
        }

        return true; // Successfully assigned the player
    }



    // Update the game data with player information
    private void updateGameWithPlayers(GameData gameData, int gameID) throws BadRequestException {
        gameDAO.updateGame(new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game()));
    }

    public void clear() {
        gameDAO.clear();
    }


}
