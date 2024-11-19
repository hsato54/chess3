package ui;


import java.util.Scanner;
import static java.lang.System.out;


public class PreloginUI {

    ServerFacade server;
    PostloginUI postloginUI;

    public PreloginUI(ServerFacade server) {
        this.server = server;
        this.postloginUI = new PostloginUI(server);
    }
    public void run() {
        out.println(EscapeSequences.WHITE_KING + " Welcome to 240 chess. Type Help to get started. " + EscapeSequences.WHITE_KING);

        while (true) {
            out.print("[LOGGED_OUT] >>> ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("help")) {
                displayHelp();

            } else if (input.equalsIgnoreCase("quit")) {
                return;

            } else if (input.startsWith("login")) {
                handleLogin(input);

            } else if (input.startsWith("register")) {
                handleRegister(input);

            } else {
                out.println("Unknown command, please try again. Type 'help' for available commands");
            }
        }
    }

    private void displayHelp(){
        displayRegister();
        displayLogin();
        out.println("quit - playing chess");
        out.println("help - with possible commands");
    }
    private void handleLogin(String input){
        server.clear();
        String[] tokens = input.split(" ");
        if (tokens.length != 3){
            out.println("Invalid login. Please provide a username and password");
            displayLogin();
        }

        String username = tokens[1];
        String password = tokens[2];

        if (server.login(username, password)){
            out.println("Success!");
            postloginUI.run();
        }
        else {
            out.println("This username or password is incorrect. Please try again.");
            displayLogin();
        }

    }

    private void handleRegister(String input){
        String[] tokens = input.split(" ");
        if (tokens.length != 4){
            out.println("Invalid. Please provide a username, password, and email");
            displayRegister();
        }

        String username = tokens[1];
        String password = tokens[2];
        String email = tokens[3];

        if (server.register(username, password, email)){
            out.println("Success!");
            postloginUI.run();
        }
        else {
            out.println("Oops, this username is taken. Please try again.");
            displayRegister();
        }
    }

    private void displayRegister(){
        out.println("register <USERNAME> <PASSWORD> <EMAIL> - create an account");
    }

    private void displayLogin(){
        out.println("login <USERNAME> <PASSWORD> - to play chess");
    }

}