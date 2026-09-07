package org.JScriptor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreation;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreationHandler;
import org.JScriptor.UI.JScriptorPanel;

/**
 * Attaches a {@link ProxyWebSocketMessageHandlerWithScript} to every WebSocket
 * created by the Proxy tool.
 */
public class ProxyWebSocketCreationHandlerWithScript implements ProxyWebSocketCreationHandler {
    private final MontoyaApi api;
    private final JScriptorPanel jScriptorPanel;

    public ProxyWebSocketCreationHandlerWithScript(MontoyaApi api, JScriptorPanel jScriptorPanel) {
        this.api = api;
        this.jScriptorPanel = jScriptorPanel;
    }

    @Override
    public void handleWebSocketCreation(ProxyWebSocketCreation webSocketCreation) {
        webSocketCreation.proxyWebSocket().registerProxyMessageHandler(
                new ProxyWebSocketMessageHandlerWithScript(this.api, this.jScriptorPanel,
                        webSocketCreation.upgradeRequest()));
    }
}
