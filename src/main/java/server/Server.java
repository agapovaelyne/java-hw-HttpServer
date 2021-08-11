package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    protected final int PORT = 9999;
    protected final int THREADS_NUMBER = 64;
    protected final ExecutorService serverThreadPool;
    protected Map<String, Handler> handlers = new ConcurrentHashMap<>();
    protected final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");


    protected final static String KEY_SEPARATOR = ":";
    protected final static String NF_ERROR_TEXT = """
            HTTP/1.1 404 Not Found\r
            Content-Length: 0\r
            Connection: close\r
            \r
            """;

    protected final Handler universalHandler = (request, out) -> {
        try {
            if (!validPaths.contains(request.getPATH())) {
                out.write((NF_ERROR_TEXT).getBytes());
                out.flush();
            }

            final var filePath = Path.of(".", "public", request.getPATH());
            final var mimeType = Files.probeContentType(filePath);

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
        } catch (FileSystemException e) {
            out.write((NF_ERROR_TEXT).getBytes());
            out.flush();
        }

    };

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
                var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                var out = new BufferedOutputStream(clientSocket.getOutputStream())
        ) {

            var request = Request.parseInputStream(clientSocket.getInputStream());
            final var path = request.getPATH();

            var handler = handlers.get(request.getMETHOD() + KEY_SEPARATOR + path);
            if (handler == null) {
                universalHandler.handle(request, out);
                return;
            }

            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String requestMethod, String requestedPath, Handler handler) throws IOException {
        String key = requestMethod + KEY_SEPARATOR + requestedPath;
        if (handlers.get(key) == null) {
            handlers.put(key, handler);
        } else {
            throw new IOException(String.format("Handler for '%s %s' request already exists!", requestMethod, requestedPath));
        }
    }
}