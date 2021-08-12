
package client;

import etc.HttpMethods;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class Client {
    private final CloseableHttpClient HTTP_CLIENT;
    private final String USER_AGENT = "II HTTPClient Service";
    private final int CONNECTION_TIMEOUT = 5000;
    private final int SOCKET_TIMEOUT = 30000;
    private final boolean REDIRECT_ENABLED = false;

    public Client() {
        HTTP_CLIENT = HttpClientBuilder.create()
                .setUserAgent(USER_AGENT)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(CONNECTION_TIMEOUT)
                        .setSocketTimeout(SOCKET_TIMEOUT)
                        .setRedirectsEnabled(REDIRECT_ENABLED)
                        .build())
                .build();
    }

    public String sendRequest(HttpMethods method, String requestLine) throws IOException {

        HttpUriRequest request;
        switch (method) {
            case GET:
                request = new HttpGet(requestLine);
                break;
            case POST:
                request = new HttpPost(requestLine);
                break;
            case PUT:
                request = new HttpPut(requestLine);
                break;
            case DELETE:
                request = new HttpDelete(requestLine);
                break;
            default:
                throw new IOException("The request method is not supported by the client");
        }
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response = this.HTTP_CLIENT.execute(request);

        //Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
        //String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        //System.out.println(body);

        return response.toString();
    }
}