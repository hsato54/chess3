package ui;

import model.GameData;

import java.util.*;

import static java.lang.System.out;

public class PostloginUI {

    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);
    private HashSet<GameData> gameList;

    public PostloginUI(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        out.println("[LOGGED_IN] >>> Type 'help' to get started.");

        while (true) {
            out.print("[LOGGED_IN] >>> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("help")) {
                displayHelp();

            } else if (input.equalsIgnoreCase("logout")) {
                handleLogout();
                break;

            } else if (input.startsWith("create ")) {
                handleCreateGame(input);

            } else if (input.equalsIgnoreCase("list")) {
                handleListGames();

            } else if (input.startsWith("join ")) {
                handleJoinGame(input);

            } else if (input.startsWith("observe ")) {
                handleObserveGame(input);

            } else if (input.equalsIgnoreCase("quit")) {
                out.println("Exiting chess game...");
                break;

            } else {
                out.println("Unknown command. Type 'help' for available commands.");
            }
        }
    }

    private void displayHelp() {
        out.println("create <NAME> - create a game");
        out.println("list - list games");
        out.println("join <game number> [WHITE|BLACK] - join a game");
        out.println("observe <game number > - observe a game");
        out.println("logout - to logout");
        out.println("quit - to exit");
        out.println("help - to display this menu again");
    }

    private void handleLogout() {
        if (server.logout()) {
            out.println("You have been logged out.");
        } else {
            out.println("Failed to log out. Please try again.");
        }
    }

    private void handleCreateGame(String input) {
        String[] tokens = input.split(" ", 2);
        if (tokens.length < 2) {
            out.println("Invalid command. Usage: create <NAME>");
            return;
        }
        String gameName = tokens[1];
        int gameId = server.createGame(gameName);
        if (gameId != -1) {
            out.printf("Game '%s' created! \n", gameName);
        } else {
            out.println("Failed to create game. Please try again.");
        }
    }

    private void handleListGames() {
        gameList = server.listGames();
        if (gameList == null || gameList.isEmpty()) {
            out.println("No games available.");
            return;
        }
        int index = 1;
        for (var game : gameList) {
            String gameName = game.gameName() != null ? game.gameName() : "Unnamed Game";
            String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "N/A";
            String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "N/A";
            out.printf("%d. %s - Players: %s vs %s\n", index++, gameName, whitePlayer, blackPlayer);
        }
    }

    private void handleJoinGame(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length != 3 || (!tokens[2].equalsIgnoreCase("WHITE") && !tokens[2].equalsIgnoreCase("BLACK"))) {
            out.println("Invalid command. Usage: join <NUMBER> [WHITE|BLACK]");
            return;
        }
        GameData selectedGame = getGameData(tokens);
        if (selectedGame == null) {
            return;
        }
        String color = tokens[2].toUpperCase();

        boolean success = server.joinGame(selectedGame.gameID(), color);
        if (success) {
            out.printf("Joined game '%s' as %s.\n", selectedGame.gameName(), color);
            boolean isPlayerWhite = color.equalsIgnoreCase("WHITE");
            new GameplayUI(server, selectedGame.gameID(), isPlayerWhite).displayBoard();
        } else {
            out.println("Failed to join game. Please check the game details and try again.");
        }
    }

    private GameData getGameData(String[] tokens) {
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            out.println("Invalid game number.");
            return null;
        }
        if (gameNumber < 1 || gameList == null || gameNumber > gameList.size()) {
            out.println("Invalid game number. Use 'list' to view available games.");
            return null;
        }
        List<GameData> gameDataList = new ArrayList<>(gameList);
        GameData selectedGame = gameDataList.get(gameNumber - 1);
        return selectedGame;
    }

    private void handleObserveGame(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length != 2) {
            out.println("Invalid command. Usage: observe <ID>");
            return;
        }
        GameData selectedGame = getGameData(tokens);
        if (selectedGame == null) {
            return;
        }
        out.printf("Game Name: %s\n", selectedGame.gameName());
    }
}
