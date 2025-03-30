package dev.uday.AI;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.response.Model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Ollama {
    public static OllamaAPI api;
    public static String model = "gemma3:1b";
    public static boolean isOllamaAvailable = false;

    public static ConcurrentHashMap<UUID, ArrayList<OllamaChatMessage>> chatHistory = new ConcurrentHashMap<>();
    public static boolean init() {
        try {
            api = new OllamaAPI();
            isOllamaAvailable = true;
            return true;
        } catch (Exception e) {
            System.err.println("Failed to initialize Ollama API: " + e.getMessage());
            return false;
        }
    }

    public static String generate(UUID userId, String prompt) {
        if (chatHistory.containsKey(userId)) {
            ArrayList<OllamaChatMessage> chatMessages = chatHistory.get(userId);
            chatMessages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));
        } else {
            ArrayList<OllamaChatMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));
            chatHistory.put(userId, chatMessages);
        }
        try {
            OllamaChatRequest request = new OllamaChatRequest(model, chatHistory.get(userId));

            api.setRequestTimeoutSeconds(180);

            OllamaChatResult result = api.chat(request);
            String response = result.getResponse();
            chatHistory.get(userId).add(new OllamaChatMessage(OllamaChatMessageRole.ASSISTANT, response));
            return response;
        } catch (Exception e) {
            System.err.println("Error generating response: " + e.getMessage());
            return "Error generating response.";
        }
    }

    // Temp main
    public static void main(String[] args) throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {

        if (!init()) {
            System.err.println("Failed to initialize Ollama API.");
            System.exit(0);
        }

        // print models
        System.out.println("Available models:");
        List<Model> models = api.listModels();
        for (Model model : models) {
            System.out.println(model.getName());
        }

        UUID userId = UUID.randomUUID();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your message (type 'exit' to quit):");
        while (true) {
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("exit")) {
                break;
            }
            String response = generate(userId, userInput);
            System.out.println("Ollama: " + response);
        }
    }
}