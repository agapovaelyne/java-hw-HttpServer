
import server.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();

        server.addHandler("GET", "/classic.html", (request, out) -> {

            final var filePath = Path.of(".", "public", request.getPATH());
            final var mimeType = Files.probeContentType(filePath);
            System.out.println(filePath);
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
        });

        server.run();
    }
}
