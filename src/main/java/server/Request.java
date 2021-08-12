package server;

import etc.HttpMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static etc.HttpMethods.*;

public class Request {
    private final HttpMethods METHOD;
    private final String PATH;
    private final Map<String, String> HEADERS;
    private final Map<String, String> PARAMS;
    private final String body;
    private final InputStream in;

    private final static String HEADERS_SEPARARTOR = ": ";
    private final static String QUERY_MARKER = "?";
    private final static String QUERY_SEPARATOR = "&";
    private final static String PARAM_SEPARATOR = "=";

    private Request(String method, String path, Map<String, String> headers, InputStream in, String body, Map<String, String> params) {
        this.METHOD = HttpMethods.valueOf(method);
        this.PATH = path;
        this.HEADERS = headers;
        this.in = in;
        this.body = body;
        this.PARAMS = params;
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

        Map<String,String> queryParams = new HashMap<>();
        if (path.contains(QUERY_MARKER)) {
            path = path.substring(0, path.indexOf(QUERY_MARKER));
            String [] queryParamPairs = path.substring(path.indexOf(QUERY_MARKER)+1).split(QUERY_SEPARATOR);
            for (String param:queryParamPairs) {
                String key = param.substring(0, param.indexOf(PARAM_SEPARATOR));
                String value = param.substring(param.indexOf(PARAM_SEPARATOR)+1);
                queryParams.put(key,value);
            }
        }

        Map<String, String> headers = new HashMap<>();
        String reqLine;
        while (!(reqLine = in.readLine()).equals("")) {
            String[] headerParts = reqLine.split(HEADERS_SEPARARTOR);
            headers.put(headerParts[0], headerParts[1]);
        }

        String body = "";

        if (!method.equals(GET)) {
            StringBuilder bodyBuilder = new StringBuilder();
            while ((reqLine = in.readLine()) != null) {
                bodyBuilder.append(reqLine);
            }
            body = bodyBuilder.toString();
        }

        return new Request(method, path, headers, inputStream, body, queryParams);
    }

    public HttpMethods getMETHOD() {
        return METHOD;
    }

    public String getPATH() {
        return PATH;
    }

    public Map<String, String> getPARAMS() {
        return PARAMS;
    }
}
