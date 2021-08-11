package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    protected final int PORT = 9999;
    protected final int THREADS_NUMBER = 64;
    protected final ExecutorService serverThreadPool;
    protected final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    protected final static String NF_ERROR_TEXT = """
            HTTP/1.1 404 Not Found\r
            Content-Length: 0\r
            Connection: close\r
            \r
            """;

    public Server() {
        serverThreadPool = Executors.newFixedThreadPool(THREADS_NUMBER);
    }

    public void run() {

        try (final var serverSocket = new ServerSocket(PORT)) {
            while (true) {
                final var socket = serverSocket.accept();
                serverThreadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (
                clientSocket;
                final var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final var out = new BufferedOutputStream(clientSocket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                out.write((NF_ERROR_TEXT).getBytes());
                out.flush();
                return;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                out.write((NF_ERROR_TEXT).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}