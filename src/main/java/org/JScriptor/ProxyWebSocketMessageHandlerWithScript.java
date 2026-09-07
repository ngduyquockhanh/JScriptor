package org.JScriptor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.proxy.websocket.BinaryMessageReceivedAction;
import burp.api.montoya.proxy.websocket.BinaryMessageToBeSentAction;
import burp.api.montoya.proxy.websocket.InterceptedBinaryMessage;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.proxy.websocket.ProxyMessageHandler;
import burp.api.montoya.proxy.websocket.TextMessageReceivedAction;
import burp.api.montoya.proxy.websocket.TextMessageToBeSentAction;
import org.JScriptor.Helper.WebSocketScriptRunner;
import org.JScriptor.UI.JScriptorPanel;
import org.JScriptor.model.JSWebSocketMessage;

/**
 * Runs the "WS Message Received" and "WS Message to be Sent" slots against Proxy
 * WebSocket traffic. One instance is registered per proxied WebSocket.
 */
public class ProxyWebSocketMessageHandlerWithScript implements ProxyMessageHandler {
    private final MontoyaApi api;
    private final JScriptorPanel jScriptorPanel;
    private final WebSocketScriptRunner scriptRunner;
    private final HttpRequest upgradeRequest;

    public ProxyWebSocketMessageHandlerWithScript(MontoyaApi api, JScriptorPanel jScriptorPanel, HttpRequest upgradeRequest) {
        this.api = api;
        this.jScriptorPanel = jScriptorPanel;
        this.upgradeRequest = upgradeRequest;
        this.scriptRunner = new WebSocketScriptRunner(api, jScriptorPanel);
    }

    @Override
    public TextMessageReceivedAction handleTextMessageReceived(InterceptedTextMessage interceptedTextMessage) {
        JSWebSocketMessage message = new JSWebSocketMessage();
        message.loadValue(interceptedTextMessage, this.upgradeRequest);

        JSWebSocketMessage result = this.scriptRunner.run(this.jScriptorPanel.getWsReceivedSlot(), message,
                this.upgradeRequest, null);

        if (result == null) {
            return TextMessageReceivedAction.continueWith(interceptedTextMessage);
        }
        if (result.isDrop()) {
            return TextMessageReceivedAction.drop();
        }
        return TextMessageReceivedAction.continueWith(result.getTextPayload());
    }

    @Override
    public TextMessageToBeSentAction handleTextMessageToBeSent(InterceptedTextMessage interceptedTextMessage) {
        JSWebSocketMessage message = new JSWebSocketMessage();
        message.loadValue(interceptedTextMessage, this.upgradeRequest);

        JSWebSocketMessage result = this.scriptRunner.run(this.jScriptorPanel.getWsToBeSentSlot(), message,
                this.upgradeRequest, null);

        if (result == null) {
            return TextMessageToBeSentAction.continueWith(interceptedTextMessage);
        }
        if (result.isDrop()) {
            return TextMessageToBeSentAction.drop();
        }
        return TextMessageToBeSentAction.continueWith(result.getTextPayload());
    }

    @Override
    public BinaryMessageReceivedAction handleBinaryMessageReceived(InterceptedBinaryMessage interceptedBinaryMessage) {
        JSWebSocketMessage message = new JSWebSocketMessage();
        message.loadValue(interceptedBinaryMessage, this.upgradeRequest);

        JSWebSocketMessage result = this.scriptRunner.run(this.jScriptorPanel.getWsReceivedSlot(), message,
                this.upgradeRequest, null);

        if (result == null) {
            return BinaryMessageReceivedAction.continueWith(interceptedBinaryMessage);
        }
        if (result.isDrop()) {
            return BinaryMessageReceivedAction.drop();
        }
        return BinaryMessageReceivedAction.continueWith(result.getBinaryPayload());
    }

    @Override
    public BinaryMessageToBeSentAction handleBinaryMessageToBeSent(InterceptedBinaryMessage interceptedBinaryMessage) {
        JSWebSocketMessage message = new JSWebSocketMessage();
        message.loadValue(interceptedBinaryMessage, this.upgradeRequest);

        JSWebSocketMessage result = this.scriptRunner.run(this.jScriptorPanel.getWsToBeSentSlot(), message,
                this.upgradeRequest, null);

        if (result == null) {
            return BinaryMessageToBeSentAction.continueWith(interceptedBinaryMessage);
        }
        if (result.isDrop()) {
            return BinaryMessageToBeSentAction.drop();
        }
        return BinaryMessageToBeSentAction.continueWith(result.getBinaryPayload());
    }
}
