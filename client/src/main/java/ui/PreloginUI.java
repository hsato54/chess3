package ui;


import java.util.Scanner;

import static java.lang.System.out;
public class PreloginUI {

    ServerFacade server;
    PostloginUI postloginUI;

    public PreloginREPL(ServerFacade server) {
        this.server = server;
        postloginUI = new PostloginUI(server);
    }
    public void run() {
        out.println(EscapeSequences.WHITE_KING + " Welcome to 240 chess. Type Help to get started. " + EscapeSequences.WHITE_KING);

        int x = 0;
        while (x == 0) {
            out.print("[LOGGED_OUT] >>> ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("help")) {
                displayHelp();

            } else if (input.equalsIgnoreCase("quit")) {
                return;

            } else if (input.toLowerCase().startsWith("login")) {
                handleLogin(input);

            } else if (input.toLowerCase().startsWith("register")) {
                handleRegister(input);

            } else {
                out.println("Unknown command, please try again. Type 'help' for available commands");
            }
        }
        postloginUI.run();
    }

    private void displayHelp(){
        displayRegister();
        displayLogin();
        out.println("quit - playing chess");
        out.println("help - with possible commands");
    }
    private void handleLogin(input){
        if (input.length != 3){
            out.println("Invalid login. Please provide a username and password");
            displayLogin();
        }
        if (server.login(input[1], input[2])){
            out.println("Success!");
            x += 1;
        }
        else {
            out.println("This username or password is incorrect. Please try again.");
            displayLogin();
        }

    }

    private void handleRegister(){
        if (input.length != 4){
            out.println("Invalid. Please provide a username, password, and email");
            displayRegister();
        }
        if (server.register(input[1], input[2], input[3])){
            out.println("Success!");
            x += 1;
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
