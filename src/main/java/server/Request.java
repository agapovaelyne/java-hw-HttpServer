package server;

import etc.HttpMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static etc.HttpMethods.GET;

public class Request {
    private final HttpMethods METHOD;
    private final String PATH;
    private final Map<String, String> HEADERS;
    private final Map<String, String> PARAMS;
    private final String body;
    private final InputStream in;

    private final static String HEADERS_SEPARATOR = ": ";
    private final static String QUERY_MARKER = "?";
    private final static String QUERY_SEPARATOR = "&";
    private final static String PARAM_SEPARATOR = "=";

    private Request(HttpMethods method, String path, Map<String, String> headers, InputStream in, String body, Map<String, String> params) {
        this.METHOD = method;
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

        var method = HttpMethods.valueOf(parts[0]);
        var path = parts[1];

        Map<String, String> params = new HashMap<>();

        if (path.contains(QUERY_MARKER)) {
            params = parseQuery(path.substring(path.indexOf(QUERY_MARKER) + 1));
            path = path.substring(0, path.indexOf(QUERY_MARKER));
        }

        Map<String, String> headers = new HashMap<>();
        String reqLine;
        while (!(reqLine = in.readLine()).equals("")) {
            String[] headerParts = reqLine.split(HEADERS_SEPARATOR);
            headers.put(headerParts[0], headerParts[1]);
        }

        String body = null;

        if (!method.equals(GET)) {
            StringBuilder bodyBuilder = new StringBuilder();
            while ((reqLine = in.readLine()) != null) {
                bodyBuilder.append(reqLine);
            }
            body = bodyBuilder.toString();
            if (headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
                Map<String, String> bodyParams = parseBody(bodyBuilder.toString());
                params.putAll(bodyParams);
            }
        }
        return new Request(method, path, headers, inputStream, body, params);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> queryParams = new HashMap<>();
        String[] queryParamPairs = query.split(QUERY_SEPARATOR);
        for (String param : queryParamPairs) {
            String key = param.substring(0, param.indexOf(PARAM_SEPARATOR));
            String value = param.substring(param.indexOf(PARAM_SEPARATOR) + 1);
            queryParams.put(key, value);
        }
        return queryParams;
    }

    private static Map<String, String> parseBody(String body) {
        body = URLDecoder.decode(body);
        Map<String, String> bodyParams = bodyParams = Arrays.stream(body.split(QUERY_SEPARATOR))
                .map(x -> x.split(PARAM_SEPARATOR))
                .collect(Collectors.toMap(x -> x[0],y -> y[1], (x,y) -> x , HashMap::new));
        return bodyParams;
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
