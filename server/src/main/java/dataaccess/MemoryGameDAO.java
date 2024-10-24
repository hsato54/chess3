package dataaccess;

import model.GameData;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;


public class MemoryGameDAO implements GameDAO {

    private Set<GameData> games;

    public MemoryGameDAO(){
        games = new HashSet<>();
    }

    @Override
    public void createGame(GameData game) throws DataAccessException{
        if (!games.contains(game)){
            games.add(game);
            return;
        }
        throw new DataAccessException("This Game Already Exists.");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("Game not found.");
    }
    @Override
    public void updateGame(GameData updatedGame) {
        games.removeIf(game -> game.gameID() == updatedGame.gameID());
        games.add(updatedGame);
    }

    @Override
    public void deleteGame(int gameID) {
        games.removeIf(game -> game.gameID() == gameID);
    }

    @Override
    public void clear() {
        games.clear();
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games);
    }
    @Override
    public boolean gameExists(int gameID) {
        return games.stream().anyMatch(game -> game.gameID() == gameID);
    }



}