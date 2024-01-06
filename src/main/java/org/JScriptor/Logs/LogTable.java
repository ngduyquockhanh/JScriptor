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

}

