package dev.uday.NET;

import dev.uday.Client;
import dev.uday.Clients;
import dev.uday.NET.Packets.PacketHandler;

import javax.crypto.*;
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
        if (port != 0) {
            PORT = port;
        }
        try {
            serverSocket = new ServerSocket(PORT);
            ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server started on port " + PORT);
            keyPair = generateKeyPair();
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
                UUID uuid = generateUniqueUUID();
                currentClients.put(uuid, new Client(clientSocket, uuid));
                System.out.println("UUID: " + uuid);

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

    private UUID generateUniqueUUID() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (currentClients.containsKey(uuid));
        return uuid;
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
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
            } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | IllegalBlockSizeException |
                     BadPaddingException | InvalidKeyException e) {
                e.printStackTrace();
            } finally {
                closeClientSocket();
            }
        }

        private void auth() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            username = decryptData(inputStream);
            password = decryptData(inputStream);
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);

            if (Clients.registeredClients.containsKey(username)) {
                if (isClientLoggedIn(username)) {
                    outputStream.writeInt(3);
                    closeClientSocket();
                } else if (Clients.registeredClients.get(username).equals(password)) {
                    outputStream.writeInt(1);
                    currentClients.get(uuid).username = username;
                    System.out.println("Client logged in: " + username);
                } else {
                    outputStream.writeInt(2);
                    closeClientSocket();
                }
            } else {
                outputStream.writeInt(0);
                closeClientSocket();
            }
        }

        private String decryptData(DataInputStream inputStream) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            int length = inputStream.readInt();
            byte[] dataBytes = new byte[length];
            inputStream.readFully(dataBytes);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return new String(cipher.doFinal(dataBytes));
        }

        private void keyExchange() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
            byte[] publicKeyBytes = publicKey.getEncoded();
            outputStream.writeInt(publicKeyBytes.length);
            outputStream.write(publicKeyBytes);
            outputStream.flush();

            int length = inputStream.readInt();
            byte[] receivedClientPublicKeyBytes = new byte[length];
            inputStream.readFully(receivedClientPublicKeyBytes);

            currentClients.get(uuid).publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(receivedClientPublicKeyBytes));
        }

        public void sendPacket(byte[] bytes) {
            try {
                int chunkSize = 240;
                int totalChunks = (int) Math.ceil((double) bytes.length / chunkSize);

                cipher.init(Cipher.ENCRYPT_MODE, currentClients.get(uuid).publicKey);
                byte[] header = ("SIZE:" + bytes.length + ";CHUNKS:" + totalChunks).getBytes();
                byte[] encryptedHeader = cipher.doFinal(header);
                outputStream.writeInt(encryptedHeader.length);
                outputStream.write(encryptedHeader);
                outputStream.flush();

                for (int i = 0; i < totalChunks; i++) {
                    int start = i * chunkSize;
                    int end = Math.min(bytes.length, start + chunkSize);
                    byte[] chunk = new byte[end - start];
                    System.arraycopy(bytes, start, chunk, 0, end - start);

                    cipher.init(Cipher.ENCRYPT_MODE, currentClients.get(uuid).publicKey);
                    byte[] encryptedChunk = cipher.doFinal(chunk);
                    outputStream.writeInt(encryptedChunk.length);
                    outputStream.write(encryptedChunk);
                    outputStream.flush();
                }
            } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
                e.printStackTrace();
            }
        }

        public void startReceivingPacket() {
            try {
                while (true) {
                    byte[] decryptedHeader = receiveAndDecryptData();
                    String header = new String(decryptedHeader);
                    int totalSize = Integer.parseInt(header.split(";")[0].split(":")[1]);
                    int totalChunks = Integer.parseInt(header.split(";")[1].split(":")[1]);

                    ByteArrayOutputStream completePacket = new ByteArrayOutputStream();
                    for (int i = 0; i < totalChunks; i++) {
                        byte[] decryptedChunk = receiveAndDecryptData();
                        completePacket.write(decryptedChunk);
                    }

                    byte[] decryptedBytes = completePacket.toByteArray();
                    System.out.println("Received packet from " + currentClients.get(uuid).username +
                            " (size: " + decryptedBytes.length + " bytes)");
                    PacketHandler.handlePacket(decryptedBytes, uuid);
                }
            } catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            } finally {
                closeClientSocket();
            }
        }

        private byte[] receiveAndDecryptData() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            int length = inputStream.readInt();
            byte[] dataBytes = new byte[length];
            inputStream.readFully(dataBytes);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return cipher.doFinal(dataBytes);
        }

        private void closeClientSocket() {
            try {
                clientSocket.close();
                currentClients.remove(uuid);
                broadcastCurrentClients();
                this.interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isClientLoggedIn(String username) {
            return currentClients.values().stream().anyMatch(client -> client.username.equals(username));
        }
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