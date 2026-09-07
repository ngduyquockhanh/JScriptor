package org.JScriptor.UI;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.contextmenu.WebSocketMessage;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.EditorMode;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;
import burp.api.montoya.websocket.Direction;
import org.JScriptor.Helper.WebSocketScriptRunner;
import org.JScriptor.model.JSWebSocketMessage;

import java.awt.*;

/**
 * A "JScriptor" tab inside Burp's WebSocket message viewer.
 * <p>
 * The Decode script turns the raw payload into something readable when a message
 * is opened; the Encode script turns whatever the user typed back into the wire
 * format when the message is sent. This is the equivalent of PyCript's WebSocket
 * editor, with JScriptor's own JavaScript engines behind it.
 */
public class JScriptorWebSocketEditor implements ExtensionProvidedWebSocketMessageEditor {
    private final MontoyaApi montoyaApi;
    private final JScriptorPanel jScriptorPanel;
    private final WebSocketScriptRunner scriptRunner;
    private final RawEditor rawEditor;

    private WebSocketMessage currentMessage;

    public JScriptorWebSocketEditor(MontoyaApi montoyaApi, JScriptorPanel jScriptorPanel,
                                    EditorCreationContext creationContext) {
        this.montoyaApi = montoyaApi;
        this.jScriptorPanel = jScriptorPanel;
        this.scriptRunner = new WebSocketScriptRunner(montoyaApi, jScriptorPanel);
        this.rawEditor = creationContext.editorMode() == EditorMode.READ_ONLY
                ? montoyaApi.userInterface().createRawEditor(EditorOptions.READ_ONLY)
                : montoyaApi.userInterface().createRawEditor();
    }

    @Override
    public void setMessage(WebSocketMessage message) {
        this.currentMessage = message;

        if (message == null) {
            this.rawEditor.setContents(ByteArray.byteArray(new byte[0]));
            return;
        }

        JSWebSocketMessage jsWebSocketMessage = toJsMessage(message);
        JSWebSocketMessage decoded = this.scriptRunner.runScript(
                this.jScriptorPanel.getWsDecodeTextArea().getText().trim(), jsWebSocketMessage);

        if (decoded == null) {
            this.rawEditor.setContents(message.payload());
            return;
        }

        this.rawEditor.setContents(decoded.getBinaryPayload());
    }

    @Override
    public ByteArray getMessage() {
        ByteArray contents = this.rawEditor.getContents();

        String encodeScript = this.jScriptorPanel.getWsEncodeTextArea().getText().trim();
        if (encodeScript.isEmpty()) {
            return contents;
        }

        JSWebSocketMessage jsWebSocketMessage = new JSWebSocketMessage();
        jsWebSocketMessage.loadValue(contents, directionOf(this.currentMessage),
                this.jScriptorPanel.getWsEditorIsBinaryCheckBox().isSelected(),
                this.currentMessage == null ? null : this.currentMessage.upgradeRequest());

        JSWebSocketMessage encoded = this.scriptRunner.runScript(encodeScript, jsWebSocketMessage);
        if (encoded == null) {
            return contents;
        }

        return encoded.getBinaryPayload();
    }

    @Override
    public boolean isEnabledFor(WebSocketMessage message) {
        return this.jScriptorPanel.getWsEditorEnabledCheckBox().isSelected()
                && !this.jScriptorPanel.getWsDecodeTextArea().getText().trim().isEmpty();
    }

    @Override
    public String caption() {
        return "JScriptor";
    }

    @Override
    public Component uiComponent() {
        return this.rawEditor.uiComponent();
    }

    @Override
    public Selection selectedData() {
        return this.rawEditor.selection().orElse(null);
    }

    @Override
    public boolean isModified() {
        return this.rawEditor.isModified();
    }

    private JSWebSocketMessage toJsMessage(WebSocketMessage message) {
        JSWebSocketMessage jsWebSocketMessage = new JSWebSocketMessage();
        jsWebSocketMessage.loadValue(message.payload(), directionOf(message),
                this.jScriptorPanel.getWsEditorIsBinaryCheckBox().isSelected(), message.upgradeRequest());
        return jsWebSocketMessage;
    }

    private Direction directionOf(WebSocketMessage message) {
        return message == null ? Direction.CLIENT_TO_SERVER : message.direction();
    }
}
