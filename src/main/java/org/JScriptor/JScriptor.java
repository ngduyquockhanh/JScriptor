package org.JScriptor;
import burp.api.montoya.*;
import org.JScriptor.UI.JScriptorPanel;

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
    }
}
