package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {
    final private String METHOD;
    final private String PATH;
    final private Map<String, String> headers;
    final private String body;
    final private InputStream in;

    private Request(String method, String path, Map<String, String> headers, InputStream in, String body) {
        this.METHOD = method;
        this.PATH = path;
        this.headers = headers;
        this.in = in;
        this.body = body;
    }

    protected static Request parseInputStream(InputStream inputStream) throws IOException {
        final var in = new BufferedReader(new InputStreamReader(inputStream));

        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            throw new IOException("Incorrect request!");
        }

        var method = parts[0];
        var path = parts[1];

        Map<String, String> headers = new HashMap<>();
        String reqLine;
        while (!(reqLine = in.readLine()).equals("")) {
            String[] headerParts = reqLine.split(": ");
            headers.put(headerParts[0], headerParts[1]);
        }

        String body = "";

        if (!method.equals("GET")) {
            StringBuilder bodyBuilder = new StringBuilder();
            while ((reqLine = in.readLine()) != null) {
                bodyBuilder.append(reqLine);
            }
            body = bodyBuilder.toString();
        }

        return new Request(method, path, headers, inputStream, body);
    }


    public String getMETHOD() {
        return METHOD;
    }

    public String getPATH() {
        return PATH;
    }
}
