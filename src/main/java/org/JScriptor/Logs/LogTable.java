package org.JScriptor.Logs;

import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.HashMap;

public class LogTable extends JTable {
    private static final long serialVersionUID = 1;

    private LogTableModel logTableModel;
    private HashMap<Integer, LogEntry> logEntryHashMap;
    private HttpRequestEditor originalRequest;
    private HttpResponseEditor originalResponse;
    private HttpRequestEditor modifiedRequest;
    private HttpResponseEditor modifiedResponse;

    public LogTable(TableModel tableModel, HashMap<Integer, LogEntry> logEntryHashMap, HttpRequestEditor originalRequest, HttpResponseEditor originalResponse,
                    HttpRequestEditor modifiedRequest, HttpResponseEditor modifiedResponse) {
        super(tableModel);
        this.logTableModel = (LogTableModel) tableModel;
        this.logEntryHashMap = logEntryHashMap;
        this.originalRequest = originalRequest;
        this.originalResponse = originalResponse;
        this.modifiedRequest = modifiedRequest;
        this.modifiedResponse = modifiedResponse;
    }

    public LogTableModel getLogTableModel() {
        return logTableModel;
    }

    public void setLogTableModel(LogTableModel logTableModel) {
        this.logTableModel = logTableModel;
    }

    public HashMap<Integer, LogEntry> getLogEntryHashMap() {
        return logEntryHashMap;
    }

    public void setLogEntryHashMap(HashMap<Integer, LogEntry> logEntryHashMap) {
        this.logEntryHashMap = logEntryHashMap;
    }

    public HttpRequestEditor getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(HttpRequestEditor originalRequest) {
        this.originalRequest = originalRequest;
    }

    public HttpResponseEditor getOriginalResponse() {
        return originalResponse;
    }

    public void setOriginalResponse(HttpResponseEditor originalResponse) {
        this.originalResponse = originalResponse;
    }

    public HttpRequestEditor getModifiedRequest() {
        return modifiedRequest;
    }

    public void setModifiedRequest(HttpRequestEditor modifiedRequest) {
        this.modifiedRequest = modifiedRequest;
    }

    public HttpResponseEditor getModifiedResponse() {
        return modifiedResponse;
    }

    public void setModifiedResponse(HttpResponseEditor modifiedResponse) {
        this.modifiedResponse = modifiedResponse;
    }
}

