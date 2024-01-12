package org.JScriptor.model;

import burp.api.montoya.MontoyaApi;

import java.util.ArrayList;
import java.util.List;

public class JSLogger {
    private List<String> logs;

    public JSLogger() {
        logs = new ArrayList<>();
    }

    public void printLogs(MontoyaApi montoyaApi){
        if (!logs.isEmpty()){
            for (String log: getLogs()){
                montoyaApi.logging().logToOutput(log);
            }
        }
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
