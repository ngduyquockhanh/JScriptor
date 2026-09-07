package org.JScriptor.Helper;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolSource;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.requests.HttpRequest;
import org.JScriptor.UI.JScriptorPanel;
import org.JScriptor.UI.ScriptSlot;
import org.JScriptor.model.JSWebSocketMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies a {@link ScriptSlot}'s filters to a WebSocket message and, if they all
 * pass, runs the slot's script on the currently selected engine.
 * <p>
 * Shared by the Proxy handler and the all-tools handler so the six WebSocket
 * callbacks behave identically.
 */
public class WebSocketScriptRunner {
    private final MontoyaApi montoyaApi;
    private final JScriptorPanel jScriptorPanel;

    public WebSocketScriptRunner(MontoyaApi montoyaApi, JScriptorPanel jScriptorPanel) {
        this.montoyaApi = montoyaApi;
        this.jScriptorPanel = jScriptorPanel;
    }

    /**
     * @param slot           the hook point being evaluated.
     * @param message        the message as seen by the script.
     * @param upgradeRequest the HTTP request that created the WebSocket, used for the scope filter.
     * @param toolSource     the tool that owns the WebSocket, used for the "not from Proxy" filter.
     *
     * @return the message the script produced, or {@code null} when the slot is
     * disabled, a filter rejected the message, or the script failed. In every
     * {@code null} case the caller must pass the original message through.
     */
    public JSWebSocketMessage run(ScriptSlot slot, JSWebSocketMessage message, HttpRequest upgradeRequest,
                                  ToolSource toolSource) {
        if (!slot.isEnabled()) {
            return null;
        }

        String script = slot.getScript();
        if (script.isEmpty()) {
            return null;
        }

        if (slot.isInScopeOnly() && upgradeRequest != null && !this.montoyaApi.scope().isInScope(upgradeRequest.url())) {
            return null;
        }

        if (slot.isSkippingProxy() && toolSource != null && toolSource.isFromTool(ToolType.PROXY)) {
            return null;
        }

        if (slot.isMatchRegexOnly()) {
            try {
                Pattern pattern = Pattern.compile(slot.getRegex());
                Matcher matcher = pattern.matcher(message.getPayload() == null ? "" : message.getPayload());
                if (!matcher.find()) {
                    return null;
                }
            } catch (Exception e) {
                this.montoyaApi.logging().logToError(e.getMessage());
                return null;
            }
        }

        return runScript(script, message);
    }

    /**
     * Runs a script on the selected engine without applying any filter. Used by
     * the WebSocket message editor, whose decode/encode slots have no filters.
     */
    public JSWebSocketMessage runScript(String script, JSWebSocketMessage message) {
        if (script == null || script.trim().isEmpty()) {
            return null;
        }

        if (this.jScriptorPanel.getRunWithGraaljs().isSelected()) {
            GraalJSEngine graalJSEngine = new GraalJSEngine(this.montoyaApi, this.jScriptorPanel,
                    null, null, null, null, script);
            return graalJSEngine.handleWebSocketMessage(message);
        }

        JavetEngine javetEngine = new JavetEngine(this.montoyaApi, this.jScriptorPanel,
                null, null, null, null, script);
        return javetEngine.handleWebSocketMessage(message);
    }
}
