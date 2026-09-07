package org.JScriptor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolSource;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.websocket.BinaryMessage;
import burp.api.montoya.websocket.BinaryMessageAction;
import burp.api.montoya.websocket.MessageHandler;
import burp.api.montoya.websocket.TextMessage;
import burp.api.montoya.websocket.TextMessageAction;
import org.JScriptor.Helper.WebSocketScriptRunner;
import org.JScriptor.UI.JScriptorPanel;
import org.JScriptor.model.JSWebSocketMessage;

/**
 * Runs the "WS Message (All Tools)" slot. Registered on every WebSocket Burp
 * creates, so it also sees Repeater traffic - and Proxy traffic, unless the slot's
 * "Don't modify message from Proxy" filter is ticked.
 */
public class WebSocketMessageHandlerWithScript implements MessageHandler {
    private final MontoyaApi api;
    private final JScriptorPanel jScriptorPanel;
    private final WebSocketScriptRunner scriptRunner;
    private final HttpRequest upgradeRequest;
    private final ToolSource toolSource;

    public WebSocketMessageHandlerWithScript(MontoyaApi api, JScriptorPanel jScriptorPanel,
                                             HttpRequest upgradeRequest, ToolSource toolSource) {
        this.api = api;
        this.jScriptorPanel = jScriptorPanel;
        this.upgradeRequest = upgradeRequest;
        this.toolSource = toolSource;
        this.scriptRunner = new WebSocketScriptRunner(api, jScriptorPanel);
    }

    @Override
    public TextMessageAction handleTextMessage(TextMessage textMessage) {
        JSWebSocketMessage message = new JSWebSocketMessage();
        message.loadValue(textMessage, this.upgradeRequest);

        JSWebSocketMessage result = this.scriptRunner.run(this.jScriptorPanel.getWsAllToolsSlot(), message,
                this.upgradeRequest, this.toolSource);

        if (result == null) {
            return TextMessageAction.continueWith(textMessage);
        }
        if (result.isDrop()) {
            return TextMessageAction.drop();
        }
        return TextMessageAction.continueWith(result.getTextPayload());
    }

    @Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        JSWebSocketMessage message = new JSWebSocketMessage();
        message.loadValue(binaryMessage, this.upgradeRequest);

        JSWebSocketMessage result = this.scriptRunner.run(this.jScriptorPanel.getWsAllToolsSlot(), message,
                this.upgradeRequest, this.toolSource);

        if (result == null) {
            return BinaryMessageAction.continueWith(binaryMessage);
        }
        if (result.isDrop()) {
            return BinaryMessageAction.drop();
        }
        return BinaryMessageAction.continueWith(result.getBinaryPayload());
    }
}
