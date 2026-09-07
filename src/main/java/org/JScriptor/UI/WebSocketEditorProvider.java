package org.JScriptor.UI;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;
import burp.api.montoya.ui.editor.extension.WebSocketMessageEditorProvider;

/**
 * Hands Burp a {@link JScriptorWebSocketEditor} whenever a WebSocket message
 * editor is created.
 */
public class WebSocketEditorProvider implements WebSocketMessageEditorProvider {
    private final MontoyaApi montoyaApi;
    private final JScriptorPanel jScriptorPanel;

    public WebSocketEditorProvider(MontoyaApi montoyaApi, JScriptorPanel jScriptorPanel) {
        this.montoyaApi = montoyaApi;
        this.jScriptorPanel = jScriptorPanel;
    }

    @Override
    public ExtensionProvidedWebSocketMessageEditor provideMessageEditor(EditorCreationContext creationContext) {
        return new JScriptorWebSocketEditor(this.montoyaApi, this.jScriptorPanel, creationContext);
    }
}
