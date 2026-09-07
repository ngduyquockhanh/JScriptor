package org.JScriptor.UI;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;

/**
 * Bundles everything that makes up one scriptable hook point: the editor holding
 * the script, its filters, the checkbox that enables it and the persistence key
 * its code is stored under.
 * <p>
 * Filter checkboxes are optional - a {@code null} filter is simply never applied.
 */
public class ScriptSlot {
    private final String name;
    private final String persistenceKey;
    private final JPanel panel;
    private final RSyntaxTextArea scriptTextArea;
    private final RSyntaxTextArea regexTextArea;
    private final JCheckBox enabledCheckBox;
    private final JCheckBox inScopeCheckBox;
    private final JCheckBox matchRegexCheckBox;
    private final JCheckBox notFromProxyCheckBox;

    public ScriptSlot(String name, String persistenceKey, JPanel panel, RSyntaxTextArea scriptTextArea,
                      RSyntaxTextArea regexTextArea, JCheckBox enabledCheckBox, JCheckBox inScopeCheckBox,
                      JCheckBox matchRegexCheckBox, JCheckBox notFromProxyCheckBox) {
        this.name = name;
        this.persistenceKey = persistenceKey;
        this.panel = panel;
        this.scriptTextArea = scriptTextArea;
        this.regexTextArea = regexTextArea;
        this.enabledCheckBox = enabledCheckBox;
        this.inScopeCheckBox = inScopeCheckBox;
        this.matchRegexCheckBox = matchRegexCheckBox;
        this.notFromProxyCheckBox = notFromProxyCheckBox;
    }

    public boolean isEnabled() {
        return this.enabledCheckBox != null && this.enabledCheckBox.isSelected();
    }

    public String getScript() {
        return this.scriptTextArea == null ? "" : this.scriptTextArea.getText().trim();
    }

    public String getRegex() {
        return this.regexTextArea == null ? "" : this.regexTextArea.getText().trim();
    }

    public boolean isInScopeOnly() {
        return this.inScopeCheckBox != null && this.inScopeCheckBox.isSelected();
    }

    public boolean isMatchRegexOnly() {
        return this.matchRegexCheckBox != null && this.matchRegexCheckBox.isSelected();
    }

    public boolean isSkippingProxy() {
        return this.notFromProxyCheckBox != null && this.notFromProxyCheckBox.isSelected();
    }

    public String getName() {
        return name;
    }

    public String getPersistenceKey() {
        return persistenceKey;
    }

    public JPanel getPanel() {
        return panel;
    }

    public RSyntaxTextArea getScriptTextArea() {
        return scriptTextArea;
    }

    public RSyntaxTextArea getRegexTextArea() {
        return regexTextArea;
    }

    public JCheckBox getEnabledCheckBox() {
        return enabledCheckBox;
    }

    public JCheckBox getInScopeCheckBox() {
        return inScopeCheckBox;
    }

    public JCheckBox getMatchRegexCheckBox() {
        return matchRegexCheckBox;
    }

    public JCheckBox getNotFromProxyCheckBox() {
        return notFromProxyCheckBox;
    }
}
