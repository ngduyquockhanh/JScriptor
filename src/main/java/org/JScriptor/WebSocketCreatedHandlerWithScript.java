package org.JScriptor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.WebSocketCreated;
import burp.api.montoya.websocket.WebSocketCreatedHandler;
import org.JScriptor.UI.JScriptorPanel;

/**
 * Attaches a {@link WebSocketMessageHandlerWithScript} to every WebSocket created
 * by any Burp tool.
 */
public class WebSocketCreatedHandlerWithScript implements WebSocketCreatedHandler {
    private final MontoyaApi api;
    private final JScriptorPanel jScriptorPanel;

    public WebSocketCreatedHandlerWithScript(MontoyaApi api, JScriptorPanel jScriptorPanel) {
        this.api = api;
        this.jScriptorPanel = jScriptorPanel;
    }

    @Override
    public void handleWebSocketCreated(WebSocketCreated webSocketCreated) {
        webSocketCreated.webSocket().registerMessageHandler(
                new WebSocketMessageHandlerWithScript(this.api, this.jScriptorPanel,
                        webSocketCreated.upgradeRequest(), webSocketCreated.toolSource()));
    }
}
