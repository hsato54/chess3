package ui;

import java.util.Scanner;

import static java.lang.System.out;

public class PostloginUI {

    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);

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
        out.println("join <ID> [WHITE|BLACK] - join a game");
        out.println("observe <ID> - observe a game");
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
            out.printf("Game '%s' created with ID %d\n", gameName, gameId);
        } else {
            out.println("Failed to create game. Please try again.");
        }
    }

    private void handleListGames() {
        var games = server.listGames();
        if (games == null || games.isEmpty()) {
            out.println("No games available.");
            return;
        }
        int index = 1;
        for (var game : games) {
            String gameName = game.gameName() != null ? game.gameName() : "Unnamed Game";
            String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "N/A";
            String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "N/A";
            out.printf("%d. %s - Players: %s vs %s\n", index++, gameName, whitePlayer, blackPlayer);
        }
    }

    private void handleJoinGame(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length != 3 || (!tokens[2].equalsIgnoreCase("WHITE") && !tokens[2].equalsIgnoreCase("BLACK"))) {
            out.println("Invalid command. Usage: join <ID> [WHITE|BLACK]");
            return;
        }
        int gameId;
        try {
            gameId = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            out.println("Invalid game ID.");
            return;
        }
        String color = tokens[2].toUpperCase();
        boolean success = server.joinGame(gameId, color);
        if (success) {
            out.printf("Joined game %d as %s.\n", gameId, color);
        } else {
            out.println("Failed to join game. Please check the game ID or color and try again.");
        }
    }

    private void handleObserveGame(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length != 2) {
            out.println("Invalid command. Usage: observe <ID>");
            return;
        }
        int gameId;
        try {
            gameId = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            out.println("Invalid game ID.");
            return;
        }
        boolean success = server.observeGame(gameId);
        if (success) {
            out.printf("Observing game %d.\n", gameId);
        } else {
            out.println("Failed to observe game. Please check the game ID and try again.");
        }
    }
}
