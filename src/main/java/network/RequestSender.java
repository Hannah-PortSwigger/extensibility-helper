package network;

import burp.api.montoya.http.Http;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;

import java.util.Map;

import static burp.api.montoya.http.message.StatusCodeClass.*;
import static burp.api.montoya.http.message.requests.HttpRequest.httpRequestFromUrl;

public class RequestSender {
    private final Http http;
    private final Logging logger;

    public RequestSender(Http http, Logging logger) {
        this.http = http;
        this.logger = logger;
    }

    public HttpResponse sendRequest(String url, Map<String, String> headers) {
        HttpRequest request = httpRequestFromUrl(url);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            request = request.withAddedHeader(header.getKey(), header.getValue());
        }

        logger.logToOutput("Requesting " + url);
        HttpResponse response = http.sendRequest(request).response();

        if (response.isStatusCodeClass(CLASS_4XX_CLIENT_ERRORS) || response.isStatusCodeClass(CLASS_5XX_SERVER_ERRORS)) {
            String responseBody = response.bodyToString();
            String exceptionMessage = "Failed to make request to " + url + ". Error code: " + response.statusCode() + ". Error message: " + responseBody;

            logger.logToError(exceptionMessage);
            throw new IllegalStateException(exceptionMessage);
        } else if (response.isStatusCodeClass(CLASS_3XX_REDIRECTION)) {
            String redirectLocation = response.headerValue("Location");
            return sendRequest(redirectLocation, headers);
        } else {
            logger.logToOutput("Request to " + url + " successful");
            return response;
        }
    }
}