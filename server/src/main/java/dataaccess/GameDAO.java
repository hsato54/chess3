package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDAO{
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void updateGame(GameData updatedGame) throws DataAccessException;
//    void deleteGame(int gameID);
    void clear();
    List<GameData> listGames();
    boolean gameExists(int gameID) throws DataAccessException;

}
