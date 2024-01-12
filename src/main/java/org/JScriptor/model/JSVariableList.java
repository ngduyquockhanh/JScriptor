package org.JScriptor.model;

import burp.api.montoya.MontoyaApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JSVariableList {
    private Map<String, String> variables;

    public JSVariableList() {
    }

    public void loadValue(MontoyaApi montoyaApi){
        variables = new HashMap<>();
        Set<String> listKey = montoyaApi.persistence().extensionData().stringKeys();
        for (String key: listKey){
            if (!key.equals("node_modules")){
                String value = montoyaApi.persistence().extensionData().getString(key);
                variables.put(key, value);
            }
        }
    }

    public void saveVariable(MontoyaApi montoyaApi){
        Set<String> listKey = getVariables().keySet();
        for (String key : listKey) {
            if (!key.equals("node_modules")){
                montoyaApi.persistence().extensionData().setString(key, getVariables().get(key));
            }
        }
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
