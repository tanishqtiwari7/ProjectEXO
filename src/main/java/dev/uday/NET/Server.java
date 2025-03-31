package dev.uday.NET;

import dev.uday.Client;
import dev.uday.Clients;
import dev.uday.NET.Packets.PacketHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private ServerSocket serverSocket;
    public static int PORT = 2005;
    public static ConcurrentHashMap<UUID, Client> currentClients = Clients.currentClients;
    private static KeyPair keyPair;
    public static String ip;
    public static String serverName = "SoloLeveler";
    public Server(int port) {
        // port is not null then set the PORT equal to the port
        if (port != 0) {
            PORT = port;
        }
        try {
            serverSocket = new ServerSocket(PORT);
            ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server started on port " + PORT);
            keyPair = generateKeyPair();
//            broadcastThread.start();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static UUID getUUIDFromUsername(String recipientUsername) {
        for (Client client : currentClients.values()) {
            if (client.username.equals(recipientUsername)) {
                return client.uuid;
            }
        }
        return null;
    }

    public void start() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                UUID uuid;
                while (true) {
                    uuid = UUID.randomUUID();
                    if (!currentClients.containsKey(uuid)) {
                        currentClients.put(uuid, new Client(clientSocket, uuid));
                        System.out.println("UUID: " + uuid);
                        break;
                    }
                }
                // Handle client in a new thread and pass the public key, UUID, clientSocket
                ClientHandler clientHandler = new ClientHandler(clientSocket, keyPair.getPublic(), uuid);
                currentClients.get(uuid).clientHandler = clientHandler;
                clientHandler.start();
            }
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final PublicKey publicKey;
        public final UUID uuid;
        private final Cipher cipher;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        public String username;
        public String password;

        public ClientHandler(Socket socket, PublicKey publicKey, UUID uuid) throws NoSuchPaddingException, NoSuchAlgorithmException {
            this.clientSocket = socket;
            this.publicKey = publicKey;
            this.uuid = uuid;
            try {
                inputStream = new DataInputStream(clientSocket.getInputStream());
                outputStream = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            cipher = Cipher.getInstance("RSA");
        }

        @Override
        public void run() {
            try {
                keyExchange();

                auth();

                broadcastCurrentClients();

                startReceivingPacket();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException | NoSuchAlgorithmException | IllegalBlockSizeException |
                     BadPaddingException | InvalidKeyException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                    broadcastCurrentClients();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void auth() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            // Get client's username
            int length = inputStream.readInt();
            byte[] usernameBytes = new byte[length];
            inputStream.readFully(usernameBytes);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedUsernameBytes = cipher.doFinal(usernameBytes);

            // Get client's password
            length = inputStream.readInt();
            byte[] passwordBytes = new byte[length];
            inputStream.readFully(passwordBytes);
            byte[] decryptedPasswordBytes = cipher.doFinal(passwordBytes);

            username = new String(decryptedUsernameBytes);
            password = new String(decryptedPasswordBytes);
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);

            // Send login success packet
            if (Clients.registeredClients.containsKey(username)) {
                if (isClientLoggedIn(username)) {
                    // Client already logged in
                    outputStream.writeInt(3);
                    stopClientSocket();
                } else if (Clients.registeredClients.get(username).equals(password)) {
                    // Login success
                    outputStream.writeInt(1);
                    currentClients.get(uuid).username = username;
                    System.out.println("Client logged in: " + username);
                } else {
                    // Password incorrect
                    outputStream.writeInt(2);
                    stopClientSocket();
                }
            } else {
                // Username not found
                outputStream.writeInt(0);
                stopClientSocket();
            }
        }

        private void keyExchange() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
            // Send public key to client
            byte[] publicKeyBytes = publicKey.getEncoded();
            outputStream.writeInt(publicKeyBytes.length);
            outputStream.write(publicKeyBytes);
            outputStream.flush();

            // Receive Client's public key
            int length = inputStream.readInt();
            byte[] receivedClientPublicKeyBytes = new byte[length];
            inputStream.readFully(receivedClientPublicKeyBytes);

            currentClients.get(uuid).publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(receivedClientPublicKeyBytes));
        }


        public void sendPacket(byte[] bytes) {
            try {
                // Calculate total number of chunks needed
                int chunkSize = 240; // Adjust chunk size as needed
                int totalChunks = (int) Math.ceil((double) bytes.length / chunkSize);

                // Send header with total packet size and chunk count
                cipher.init(Cipher.ENCRYPT_MODE, currentClients.get(uuid).publicKey);
                byte[] header = ("SIZE:" + bytes.length + ";CHUNKS:" + totalChunks).getBytes();
                byte[] encryptedHeader = cipher.doFinal(header);
                outputStream.writeInt(encryptedHeader.length);
                outputStream.write(encryptedHeader);
                outputStream.flush();

                // Send each chunk
                for (int i = 0; i < totalChunks; i++) {
                    int start = i * chunkSize;
                    int end = Math.min(bytes.length, start + chunkSize);
                    int currentChunkSize = end - start;

                    byte[] chunk = new byte[currentChunkSize];
                    System.arraycopy(bytes, start, chunk, 0, currentChunkSize);

                    cipher.init(Cipher.ENCRYPT_MODE, currentClients.get(uuid).publicKey);
                    byte[] encryptedChunk = cipher.doFinal(chunk);
                    outputStream.writeInt(encryptedChunk.length);
                    outputStream.write(encryptedChunk);
                    outputStream.flush();
                }
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void startReceivingPacket() {
            try {
                while (true) {
                    // Receive header
                    int headerLength = inputStream.readInt();
                    byte[] headerBytes = new byte[headerLength];
                    inputStream.readFully(headerBytes);
                    cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                    byte[] decryptedHeader = cipher.doFinal(headerBytes);

                    String header = new String(decryptedHeader);
                    int totalSize = Integer.parseInt(header.split(";")[0].split(":")[1]);
                    int totalChunks = Integer.parseInt(header.split(";")[1].split(":")[1]);

                    // Receive all chunks
                    ByteArrayOutputStream completePacket = new ByteArrayOutputStream();
                    for (int i = 0; i < totalChunks; i++) {
                        int chunkLength = inputStream.readInt();
                        byte[] chunk = new byte[chunkLength];
                        inputStream.readFully(chunk);
                        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                        byte[] decryptedChunk = cipher.doFinal(chunk);
                        completePacket.write(decryptedChunk);
                    }

                    byte[] decryptedBytes = completePacket.toByteArray();
                    System.out.println("Received packet from " + currentClients.get(uuid).username +
                            " (size: " + decryptedBytes.length + " bytes)");
                    PacketHandler.handlePacket(decryptedBytes, uuid);
                }
            } catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            } finally {
                stopClientSocket();
            }
        }
        private void stopClientSocket() {
            try {
                clientSocket.close();
                // Remove client from clients map
                currentClients.remove(uuid);

                // Broadcast current clients
                broadcastCurrentClients();

                // Stop the thread
                this.interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isClientLoggedIn(String username) {
            for (Client client : currentClients.values()) {
                if (client.username.equals(username)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(byte[] bytes) {
        for (Client client : currentClients.values()) {
            client.clientHandler.sendPacket(bytes);
        }
        System.out.println("Broadcasted packet");
    }

    private static void broadcastCurrentClients() {
        if (currentClients.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Client client : currentClients.values()) {
            sb.append(client.username).append(",");
        }
        String message = sb.toString();
        byte messageType = 0;
        byte[] messageBytes = message.getBytes();
        byte[] bytes = new byte[messageBytes.length + 1];
        bytes[0] = messageType;
        System.arraycopy(messageBytes, 0, bytes, 1, messageBytes.length);
        broadcast(bytes);
        System.out.println("Broadcasted current clients");
    }

    Thread broadcastThread = new Thread(() -> {
        while (true) {
            broadcastCurrentClients();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
}