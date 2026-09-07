package org.JScriptor.model;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.websocket.BinaryMessage;
import burp.api.montoya.websocket.Direction;
import burp.api.montoya.websocket.TextMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The JavaScript view of a WebSocket message.
 * <p>
 * A text message exposes its payload as-is. A binary message exposes its payload
 * base64 encoded with {@code binary} set to {@code true}, so a single script can
 * handle both by checking the flag.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSWebSocketMessage {
    private String payload;
    private boolean binary;
    private String direction;
    private boolean drop;
    private JSHttpService httpService;
    private String path;
    private String url;

    public JSWebSocketMessage() {
    }

    public void loadValue(TextMessage textMessage, HttpRequest upgradeRequest) {
        this.payload = textMessage.payload();
        this.binary = false;
        this.direction = textMessage.direction().name();
        this.drop = false;
        loadUpgradeRequest(upgradeRequest);
    }

    public void loadValue(BinaryMessage binaryMessage, HttpRequest upgradeRequest) {
        this.payload = Base64.getEncoder().encodeToString(binaryMessage.payload().getBytes());
        this.binary = true;
        this.direction = binaryMessage.direction().name();
        this.drop = false;
        loadUpgradeRequest(upgradeRequest);
    }

    /**
     * Used by the WebSocket message editor, where only the raw payload and the
     * direction are known.
     */
    public void loadValue(ByteArray payload, Direction direction, boolean binary, HttpRequest upgradeRequest) {
        this.payload = binary
                ? Base64.getEncoder().encodeToString(payload.getBytes())
                : new String(payload.getBytes(), StandardCharsets.UTF_8);
        this.binary = binary;
        this.direction = direction == null ? Direction.CLIENT_TO_SERVER.name() : direction.name();
        this.drop = false;
        loadUpgradeRequest(upgradeRequest);
    }

    private void loadUpgradeRequest(HttpRequest upgradeRequest) {
        if (upgradeRequest == null) {
            return;
        }
        this.httpService = new JSHttpService(upgradeRequest.httpService().host(), upgradeRequest.httpService().port(),
                upgradeRequest.httpService().secure());
        this.path = upgradeRequest.path();
        this.url = upgradeRequest.url();
    }

    /**
     * @return the payload the script produced, as a plain text WebSocket payload.
     */
    @JsonIgnore
    public String getTextPayload() {
        if (this.payload == null) {
            return "";
        }
        if (this.binary) {
            return new String(Base64.getDecoder().decode(this.payload), StandardCharsets.UTF_8);
        }
        return this.payload;
    }

    /**
     * @return the payload the script produced, as raw bytes. A script may flip
     * {@code binary} either way, so both encodings are handled.
     */
    @JsonIgnore
    public ByteArray getBinaryPayload() {
        if (this.payload == null) {
            return ByteArray.byteArray(new byte[0]);
        }
        if (this.binary) {
            return ByteArray.byteArray(Base64.getDecoder().decode(this.payload));
        }
        return ByteArray.byteArray(this.payload.getBytes(StandardCharsets.UTF_8));
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isDrop() {
        return drop;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    public JSHttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(JSHttpService httpService) {
        this.httpService = httpService;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "JSWebSocketMessage{" +
                "payload='" + payload + '\'' +
                ", binary=" + binary +
                ", direction='" + direction + '\'' +
                ", drop=" + drop +
                ", httpService=" + httpService +
                ", path='" + path + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
