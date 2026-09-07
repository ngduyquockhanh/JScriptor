package org.JScriptor;
import burp.api.montoya.*;
import org.JScriptor.UI.JScriptorPanel;
import org.JScriptor.UI.WebSocketEditorProvider;

public class JScriptor implements  BurpExtension{
    private MontoyaApi montoyaApi;
    private JScriptorPanel jScriptorPanel;

    @Override
    public void initialize(MontoyaApi api) {
        this.montoyaApi = api;
        api.extension().setName("JScriptor");

        this.jScriptorPanel = new JScriptorPanel(this.montoyaApi);

        api.userInterface().registerSuiteTab("JScriptor", this.jScriptorPanel.getMainSlitPaneHorizontal());

        HttpHandlerWithScript httpHandlerWithScript = new HttpHandlerWithScript(api, this.jScriptorPanel);
        api.http().registerHttpHandler(httpHandlerWithScript);

        ProxyRequestHandlerWithScript proxyHandlerWithScript = new ProxyRequestHandlerWithScript(api, this.jScriptorPanel);
        api.proxy().registerRequestHandler(proxyHandlerWithScript);

        ProxyResponseHandlerWithScript proxyResponseHandlerWithScript = new ProxyResponseHandlerWithScript(api, this.jScriptorPanel);
        api.proxy().registerResponseHandler(proxyResponseHandlerWithScript);

        ProxyWebSocketCreationHandlerWithScript proxyWebSocketCreationHandler =
                new ProxyWebSocketCreationHandlerWithScript(api, this.jScriptorPanel);
        api.proxy().registerWebSocketCreationHandler(proxyWebSocketCreationHandler);

        WebSocketCreatedHandlerWithScript webSocketCreatedHandler =
                new WebSocketCreatedHandlerWithScript(api, this.jScriptorPanel);
        api.websockets().registerWebSocketCreatedHandler(webSocketCreatedHandler);

        api.userInterface().registerWebSocketMessageEditorProvider(
                new WebSocketEditorProvider(api, this.jScriptorPanel));
    }
}
