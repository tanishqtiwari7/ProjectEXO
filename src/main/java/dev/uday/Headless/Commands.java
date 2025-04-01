package dev.uday.Headless;

import dev.uday.AI.Ollama;
import dev.uday.Clients;
import dev.uday.NET.Server;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.response.Model;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Commands implements Runnable {
    List<Model> models;
    Logger logger = org.slf4j.LoggerFactory.getLogger(Commands.class);

    @Override
    public void run() {
        while (true) {
            try {
                // print ~/ in red
                System.out.print("\u001B[31m" + " ~/ " + "\u001B[0m");
                String command = System.console().readLine();
                if (command.equals("exit")) {
                    System.out.println("Shutting down server...");
                    System.exit(0);
                    break;
                } else if (command.equals("help")) {
                    help();
                } else if (command.startsWith("ai")) {
                    ai(command);
                } else if (command.startsWith("list")) {
                    list(command);
                } else if (command.startsWith("register")) {
                    register(command);
                } else if (command.startsWith("unregister")) {
                    unregister(command);
                } else if (command.equals("info")) {
                    info();
                } else {
                    System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private static void info() {
        System.out.println("Server information:");
        System.out.println(" - Server IP: " + Server.ip);
        System.out.println(" - Server name: " + Server.serverName);
        System.out.println(" - Server port: " + Server.PORT);
    }

    private static void unregister(String command) {
        String[] commandParts = command.split(" ");
        if (commandParts.length == 2) {
            String username = commandParts[1];
            if (Clients.registeredClients.remove(username) != null) {
                System.out.println("Unregistered user: " + username);
            } else {
                System.out.println("Username not found.");
            }
        } else {
            System.out.println("Type help for a list of commands.");
        }
    }

    private static void register(String command) {
        String[] commandParts = command.split(" ");
        if (commandParts.length == 2) {
            String username = commandParts[1];
            System.out.println("Registering user: " + username);
            System.out.println("Enter password: ");
            char[] charPassword = System.console().readPassword();
            String password = new String(charPassword);
            if (Clients.registeredClients.put(username, password) == null) {
                System.out.println("Registered user: " + username);
                Clients.saveRegisteredUser(username, password);
            } else {
                System.out.println("Username already exists.");
            }
        } else {
            System.out.println("Type help for a list of commands.");
        }
    }

    private static void list(String command) {
        String[] commandParts = command.split(" ");
        if (commandParts.length == 2) {
            if (commandParts[1].equals("clients")) {
                System.out.println("Connected clients: ");
                Server.currentClients.forEach((uuid, client) -> System.out.println(" - " + client.username));
            } else if (commandParts[1].equals("registered")) {
                System.out.println("Registered clients: ");
                Clients.registeredClients.forEach((username, password) -> System.out.println(" - " + username));
            } else {
                System.out.println("Type help for a list of commands.");
            }
        } else {
            System.out.println("Type help for a list of commands.");
        }
    }

    private void ai(String command) throws OllamaBaseException, IOException, InterruptedException, URISyntaxException {
        String[] commandParts = command.split(" ");
        if (commandParts.length == 2) {
            if (commandParts[1].equals("current")) {
                System.out.println("Current AI model: " + Ollama.model);
            } else if (commandParts[1].equals("list")) {
                System.out.println("Available AI models: ");
                try {
                    models = Ollama.api.listModels();
                    models.forEach(model -> System.out.println(" - " + model.getName()));
                } catch (Exception e) {
                    System.out.println("Failed to list models api not available");
                }

            } else {
                System.out.println("Type help for a list of commands.");
            }
        } else if (commandParts.length == 3) {
            if (commandParts[1].equals("set")) {
                String model = commandParts[2];
                models = Ollama.api.listModels();
                if (models.stream().anyMatch(m -> m.getName().equals(model))) {
                    Ollama.model = model;
                    System.out.println("Set current AI model to: " + model);
                } else {
                    System.out.println("Model not found. Type 'ai list' to see available models.");
                }
            } else {
                System.out.println("Type help for a list of commands.");
            }
        } else {
            System.out.println("Type help for a list of commands.");
        }
    }

    private void help() {
        System.out.print("""
                Available commands:
                - help: Show this help message
                - exit: Shut down the server
                - list clients: List all connected clients
                       registered: List all registered clients
                - register <username>: Register a new client with the given username
                - unregister <username>: Unregister a client with the given username
                - ai current: Show the current AI model
                     list: List all available AI models
                     set <model>: Set the current AI model to the given model (from the list)
                - info: Show server information
                """);
    }
}