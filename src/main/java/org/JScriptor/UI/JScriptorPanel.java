package org.JScriptor.UI;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import org.JScriptor.Logs.LogEntry;
import org.JScriptor.Logs.LogTable;
import org.JScriptor.Logs.LogTableModel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.graalvm.polyglot.Source;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class JScriptorPanel {
    private MontoyaApi montoyaApi;
    private JSplitPane mainSlitPaneHorizontal;
    private JSplitPane aboveSplitPaneVertizontal;
    private JPanel tableRequestPanel;
    private LogTableModel logTableModel = new LogTableModel(new Object[]{"#", "METHOD", "URL", "STATUS", "ORIGINAL LENGTH"}, 0);

    private HashMap<Integer, LogEntry> logEntryHashMap;
    private HashMap<String, String> logRowHashMap;

    private JPanel extensionPanel;
    private JPanel extensionButtonPanel;
    private JButton removeLogButton;
    private JCheckBox saveLogButton;
    private JCheckBox runPrescriptButton;
    private JCheckBox runPostscriptButton;

    private JPanel configurationPanel;

    private JRadioButton runWithGraaljs;

    private JRadioButton runWithJavet;

    private JTabbedPane extensionTabbedPane;
    private JPanel prescriptPanel;
    private RSyntaxTextArea prescriptTextArea;
    private RSyntaxTextArea prescriptRegexTextArea;
    private JCheckBox prescriptIsNotModifyRequestFromProxyCheckbox;
    private JCheckBox prescriptIsInScopeCheckBox;
    private JCheckBox prescriptIsMatchRegexCheckBox;

    private JPanel postscriptPanel;
    private RSyntaxTextArea postscriptTextArea;
    private RSyntaxTextArea postscriptRegexTextArea;
    private JCheckBox postscriptIsNotModifyResponseFromProxyCheckBox;
    private JCheckBox postscriptIsMatchRegexCheckBox;


    private JPanel variablePanel;
    private JPanel pureLibraryPanel;
    private JPanel nodejsLibraryPanel;
    private DefaultTableModel pureTableModel = new DefaultTableModel(new Object[]{"File Path"}, 0);
    private ArrayList<Source> listPureLibray = new ArrayList<>();
    private DefaultTableModel nodejsTableModel = new DefaultTableModel(new Object[]{"File to node_modules"}, 0);

    private DefaultTableModel variableTableModel = new DefaultTableModel(new Object[]{"Key", "Value"}, 0);

    private JTabbedPane belowTabbedPane;
    private JSplitPane belowSplitPaneVertizontalOriginal;
    private HttpRequestEditor originalRequest;
    private HttpResponseEditor originalResponse;

    private JSplitPane belowSplitPaneVertizontalModified;
    private HttpRequestEditor modifiedRequest;
    private HttpResponseEditor modifiedResponse;

    public JScriptorPanel(MontoyaApi api) {
        this.montoyaApi = api;
        this.mainSlitPaneHorizontal = new JSplitPane(0);

        this.aboveSplitPaneVertizontal = new JSplitPane(1);

        this.tableRequestPanel = createTableLogPanel();
        this.logEntryHashMap = new HashMap<>();
        this.logRowHashMap = new HashMap<>();


        this.extensionPanel = new JPanel();
        this.extensionPanel.setLayout(new BoxLayout(this.extensionPanel,BoxLayout.Y_AXIS));

        this.extensionButtonPanel = new JPanel();
        this.extensionButtonPanel.setLayout(new BoxLayout(this.extensionButtonPanel, BoxLayout.Y_AXIS));

        this.removeLogButton = new JButton("Remove Log");
        this.saveLogButton = new JCheckBox("Save Log");
        this.runPrescriptButton = new JCheckBox("Run Pre-script");
        this.runPostscriptButton = new JCheckBox("Run Post-script");
        this.extensionButtonPanel.add(this.removeLogButton);
        this.extensionButtonPanel.add(this.saveLogButton);
        this.extensionButtonPanel.add(this.runPrescriptButton);
        this.extensionButtonPanel.add(this.runPostscriptButton);

        this.removeLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTableModel.setRowCount(0);
                logEntryHashMap.clear();
                originalRequest.setRequest(null);
                originalResponse.setResponse(null);
                modifiedRequest.setRequest(null);
                modifiedResponse.setResponse(null);
            }
        });

        this.configurationPanel = new JPanel();
        this.configurationPanel.setLayout(new BoxLayout(this.configurationPanel, BoxLayout.Y_AXIS));
        this.runWithGraaljs = new JRadioButton("Run with Graaljs");
        this.runWithGraaljs.setSelected(true);
        this.runWithJavet = new JRadioButton("Run with Javet");

        ButtonGroup group = new ButtonGroup();
        group.add(this.runWithGraaljs);
        group.add(this.runWithJavet);

        JLabel jLabel1 = new JLabel("- Graaljs can provide method to access to Java Class, and Node Module. But may get error when send many concurrent request");
        JLabel jLabel2 = new JLabel("- Javet can provide method run nodejs and can handle when send many concurrent request");

        this.configurationPanel.add(jLabel1);
        this.configurationPanel.add(jLabel2);
        this.configurationPanel.add(this.runWithGraaljs);
        this.configurationPanel.add(this.runWithJavet);


        JTabbedPane outButtonPanel = new JTabbedPane();
        outButtonPanel.add("Running", this.extensionButtonPanel);
        outButtonPanel.add("Configuration", this.configurationPanel);


        this.extensionTabbedPane = new JTabbedPane();

        this.prescriptPanel = new JPanel();
        RSyntaxTextArea first = createRSyntaxTextArea();
        this.prescriptTextArea = createRSyntaxTextArea();
        this.prescriptRegexTextArea = createRSyntaxTextArea();
        this.prescriptIsInScopeCheckBox = new JCheckBox("In Scope Item");
        this.prescriptIsMatchRegexCheckBox = new JCheckBox("Match regex");
        this.prescriptIsNotModifyRequestFromProxyCheckbox = new JCheckBox("Don't modified request from Proxy");

        this.prescriptPanel = createScriptPanel(this.prescriptTextArea, this.prescriptIsInScopeCheckBox,
                this.prescriptIsMatchRegexCheckBox, this.prescriptRegexTextArea, this.prescriptIsNotModifyRequestFromProxyCheckbox);

        this.postscriptTextArea = createRSyntaxTextArea();
        this.postscriptRegexTextArea = createRSyntaxTextArea();
        this.postscriptIsMatchRegexCheckBox = new JCheckBox("Match regex");
        this.postscriptIsNotModifyResponseFromProxyCheckBox = new JCheckBox("Don't modified response from Proxy");

        this.postscriptPanel = createScriptPanel(this.postscriptTextArea, null,
                this.postscriptIsMatchRegexCheckBox, this.postscriptRegexTextArea, this.postscriptIsNotModifyResponseFromProxyCheckBox);

        this.variablePanel = createVariableTabPanel(this.variableTableModel);
        this.pureLibraryPanel = createSettingTabPanel(this.pureTableModel);
        this.nodejsLibraryPanel = createSettingNodejsTabPanel(this.nodejsTableModel);

        this.extensionTabbedPane.add("Pre-script", this.prescriptPanel);
        this.extensionTabbedPane.add("Post-script", this.postscriptPanel);
        this.extensionTabbedPane.add("Variable Setting", this.variablePanel);
        this.extensionTabbedPane.add("Pure Javascript Library", this.pureLibraryPanel);
        this.extensionTabbedPane.add("Nodejs Library", this.nodejsLibraryPanel);

        this.extensionTabbedPane.addChangeListener(new ChangeListener() { //add the Listener
            public void stateChanged(ChangeEvent e) {

                if(extensionTabbedPane.getSelectedIndex()==2)
                {
                    variableTableModel.setRowCount(0);
                    Set<String> listKey = montoyaApi.persistence().extensionData().stringKeys();
                    for (String key: listKey){
                        if (!key.equals("node_modules")){
                            String value = montoyaApi.persistence().extensionData().getString(key);
                            variableTableModel.addRow(new Object[]{key, value});
                        }

                    }
                }
            }
        });

        this.extensionPanel.add(outButtonPanel);
        this.extensionPanel.add(this.extensionTabbedPane);


        this.aboveSplitPaneVertizontal.setLeftComponent(this.tableRequestPanel);
        this.aboveSplitPaneVertizontal.setRightComponent(this.extensionPanel);
        this.aboveSplitPaneVertizontal.setResizeWeight(0.5);


        this.belowTabbedPane = new JTabbedPane();

        this.belowSplitPaneVertizontalOriginal = new JSplitPane(1);
        this.originalRequest = this.montoyaApi.userInterface().createHttpRequestEditor();
        this.originalResponse = this.montoyaApi.userInterface().createHttpResponseEditor();
        this.belowSplitPaneVertizontalOriginal.setLeftComponent(this.originalRequest.uiComponent());
        this.belowSplitPaneVertizontalOriginal.setRightComponent(this.originalResponse.uiComponent());
        this.belowSplitPaneVertizontalOriginal.setResizeWeight(0.5);


        this.belowSplitPaneVertizontalModified =new JSplitPane(1);
        this.modifiedRequest = this.montoyaApi.userInterface().createHttpRequestEditor();
        this.modifiedResponse = this.montoyaApi.userInterface().createHttpResponseEditor();
        this.belowSplitPaneVertizontalModified.setLeftComponent(this.modifiedRequest.uiComponent());
        this.belowSplitPaneVertizontalModified.setRightComponent(this.modifiedResponse.uiComponent());
        this.belowSplitPaneVertizontalModified.setResizeWeight(0.5);

        this.belowTabbedPane.add("Original", this.belowSplitPaneVertizontalOriginal);
        this.belowTabbedPane.add("Modified", this.belowSplitPaneVertizontalModified);

        this.mainSlitPaneHorizontal.setTopComponent(this.aboveSplitPaneVertizontal);
        this.mainSlitPaneHorizontal.setBottomComponent(this.belowTabbedPane);
        this.mainSlitPaneHorizontal.setResizeWeight(0.5);

    }

    private RSyntaxTextArea createRSyntaxTextArea(){
        JTextComponent.removeKeymap("RTextAreaKeymap");
        RSyntaxTextArea rSyntaxTextArea = new RSyntaxTextArea();
        rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        try{
            if (this.montoyaApi.userInterface().currentTheme().toString().equals("DARK")){
                InputStream in = getClass().getClassLoader().getResourceAsStream("dark.xml");
                Theme theme = Theme.load(in);
                theme.apply(rSyntaxTextArea);

            }
        }catch (Exception e){
            this.montoyaApi.logging().logToError(e.getMessage());
        }
        UIManager.put("RSyntaxTextAreaUI.actionMap", null);
        UIManager.put("RSyntaxTextAreaUI.inputMap", null);
        UIManager.put("RTextAreaUI.actionMap", null);
        UIManager.put("RTextAreaUI.inputMap", null);
        return rSyntaxTextArea;
    }

    private void openInputDialog(JPanel panel, DefaultTableModel tableModel) {
        JDialog inputDialog = new JDialog(this.montoyaApi.userInterface().swingUtils().suiteFrame(), "Input variable", true);
        inputDialog.setLayout(new GridLayout(5, 1));
        inputDialog.setSize(400, 600);

        JTextField keyField = new JTextField(10);
        JTextArea valueField = new JTextArea(20,10);

        JScrollPane jScrollPane = new JScrollPane(valueField);
        inputDialog.add(new JLabel("Key:"));
        inputDialog.add(keyField);
        inputDialog.add(new JLabel("Value:"));
        inputDialog.add(jScrollPane);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = keyField.getText();
                String value = valueField.getText();
                montoyaApi.persistence().extensionData().setString(key, value);
                for (int i = 0; i < tableModel.getRowCount(); i++){
                    String key_temp = (String) tableModel.getValueAt(i, 0);
                    if (key_temp.equals(key)){
                        tableModel.setValueAt(value, i, 1);
                        inputDialog.dispose();
                        return;
                    }
                }

                tableModel.addRow(new String[]{key, value});
                inputDialog.dispose();
            }
        });
        inputDialog.add(submitButton);

        inputDialog.setVisible(true);
    }

    private JPanel createTableLogPanel(){
        JPanel panel = new JPanel(new BorderLayout());

        LogTable table = new LogTable(this.logTableModel, this.logEntryHashMap, this.originalRequest, this.originalResponse,
                this.modifiedRequest, this.modifiedResponse);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c2 = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                return c2;
            }
        });
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollTable = new JScrollPane(table);

        panel.add(scrollTable, BorderLayout.CENTER);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(5);
        table.getColumnModel().getColumn(1).setPreferredWidth(5);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);
        table.getColumnModel().getColumn(3).setPreferredWidth(10);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int viewRowIndex = table.getSelectedRow();
                    if (viewRowIndex != -1) {
                        int modelRowIndex = table.convertRowIndexToModel(viewRowIndex);
                        Integer requestId = Integer.parseInt(logTableModel.getValueAt(modelRowIndex, 0).toString());
                        originalRequest.setRequest(logEntryHashMap.get(requestId).getOriginalHttpRequest());
                        originalResponse.setResponse(logEntryHashMap.get(requestId).getOriginalHttpResponse());
                        modifiedRequest.setRequest(logEntryHashMap.get(requestId).getModifiedHttpRequest());
                        modifiedResponse.setResponse(logEntryHashMap.get(requestId).getModifiedHttpResponse());
                    }

                }
            }
        });


        return panel;
    }


    private JPanel createScriptPanel(RSyntaxTextArea script, JCheckBox inScopeCheckbox,
                                     JCheckBox regexCheckbox, RSyntaxTextArea regexTextField, JCheckBox proxyCheckbox) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Run script for specific request");

        JPanel scopePanel = new JPanel();
        scopePanel.setLayout(new BorderLayout());

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new GridLayout(4, 1));

        checkBoxPanel.add(titleLabel);
        if (inScopeCheckbox != null){
            checkBoxPanel.add(inScopeCheckbox);
            if (this.montoyaApi.persistence().extensionData().getByteArray("prescript_code") != null){
                script.setText(this.montoyaApi.persistence().extensionData().getByteArray("prescript_code").toString());
            }
        }
        else{
            if (this.montoyaApi.persistence().extensionData().getByteArray("postscript_code") != null){
                script.setText(this.montoyaApi.persistence().extensionData().getByteArray("postscript_code").toString());
            }
        }
        checkBoxPanel.add(proxyCheckbox);
        checkBoxPanel.add(regexCheckbox);



        scopePanel.add(checkBoxPanel, BorderLayout.NORTH);
        scopePanel.add(regexTextField, BorderLayout.CENTER);

        RTextScrollPane rTextScrollPane = new RTextScrollPane(script);

        panel.add(rTextScrollPane, BorderLayout.CENTER);
        panel.add(scopePanel, BorderLayout.EAST);


        return panel;
    }

    private JPanel createVariableTabPanel(DefaultTableModel tableModel){
        Set<String> listKey = montoyaApi.persistence().extensionData().stringKeys();
        for (String key: listKey){
            if (!key.equals("node_modules")){
                String value = montoyaApi.persistence().extensionData().getString(key);
                tableModel.addRow(new Object[]{key, value});
            }
        }

        JPanel panel = new JPanel(new BorderLayout());
        JButton addValue = new JButton("Add Value");
        JButton removeSelectedButton = new JButton("Remove Selected");
        JButton clearAllButton = new JButton("Clear All");

        JTable table = new JTable(tableModel);
        JScrollPane scrollTable = new JScrollPane(table);
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        buttonPanel.add(addValue);
        buttonPanel.add(removeSelectedButton);
        buttonPanel.add(clearAllButton);


        addValue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openInputDialog(panel, tableModel);
            }
        });

        removeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Delete selected row from the table
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String key = (String) tableModel.getValueAt(selectedRow, 0);
                    montoyaApi.persistence().extensionData().deleteString(key);
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < tableModel.getRowCount(); i++){
                    String key = (String) tableModel.getValueAt(i, 0);
                    montoyaApi.persistence().extensionData().deleteString(key);
                }
                tableModel.setRowCount(0);
            }
        });

        panel.add(scrollTable, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }
    private JPanel createSettingTabPanel(DefaultTableModel tableModel) {
        PersistedList<String> pureList = montoyaApi.persistence().extensionData().getStringList("pure_libraries");
        if (pureList != null){
            for (int i = 0; i < pureList.size(); i++){
                try{
                    String filepath = pureList.get(i);
                    Source librarySource = Source.newBuilder("js", new File(filepath)).build();
                    listPureLibray.add(librarySource);
                    tableModel.addRow(new String[]{filepath});
                }catch (IOException ex) {
                    montoyaApi.logging().logToError(ex.getMessage());
                }

            }
        }
        JPanel panel = new JPanel(new BorderLayout());
        JButton chooseFileButton = new JButton("Add File");
        JButton removeSelectedButton = new JButton("Remove Selected");
        JButton clearAllButton = new JButton("Clear All");

        JTable table = new JTable(tableModel);
        JScrollPane scrollTable = new JScrollPane(table);
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        buttonPanel.add(chooseFileButton);
        buttonPanel.add(removeSelectedButton);
        buttonPanel.add(clearAllButton);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int choice = fileChooser.showOpenDialog(panel);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile != null) {
                        String filePath = selectedFile.getAbsolutePath();
                        try {
                            Source librarySource = Source.newBuilder("js", new File(filePath)).build();
                            PersistedList<String> pureList = montoyaApi.persistence().extensionData().getStringList("pure_libraries");
                            if (pureList == null){
                                pureList = PersistedList.persistedStringList();
                                pureList.add(filePath);
                                montoyaApi.persistence().extensionData().setStringList("pure_libraries", pureList);
                                listPureLibray.add(librarySource);
                                tableModel.addRow(new String[]{filePath});
                            }else{
                                pureList.add(filePath);
                                montoyaApi.persistence().extensionData().setStringList("pure_libraries", pureList);
                                listPureLibray.add(librarySource);
                                tableModel.addRow(new String[]{filePath});
                            }

                        } catch (IOException ex) {
                            montoyaApi.logging().logToError(ex.getMessage());
                        }
                    }
                }
            }
        });

        removeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Delete selected row from the table
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    listPureLibray.remove(selectedRow);
                    PersistedList<String> pureList = montoyaApi.persistence().extensionData().getStringList("pure_libraries");
                    pureList.remove(selectedRow);
                    montoyaApi.persistence().extensionData().setStringList("pure_libraries", pureList);
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Clear all rows from the table
                listPureLibray.clear();
                montoyaApi.persistence().extensionData().deleteStringList("pure_libraries");
                tableModel.setRowCount(0);
            }
        });

        panel.add(scrollTable, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSettingNodejsTabPanel(DefaultTableModel tableModel) {
        String nodePath = montoyaApi.persistence().extensionData().getString("node_modules");
        if (nodePath != null){
            tableModel.addRow(new Object[]{nodePath});
        }
        JPanel panel = new JPanel(new BorderLayout());
        JButton chooseFileButton = new JButton("Add File");
        JButton clearAllButton = new JButton("Clear All");

        JTable table = new JTable(tableModel);
        JScrollPane scrollTable = new JScrollPane(table);
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        buttonPanel.add(chooseFileButton);
        buttonPanel.add(clearAllButton);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tableModel.getRowCount() == 0){
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.setAcceptAllFileFilterUsed(false);

                    int option = fileChooser.showOpenDialog(null);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        String nodePath = fileChooser.getSelectedFile().getAbsolutePath();
                        montoyaApi.persistence().extensionData().setString("node_modules", nodePath);
                        tableModel.addRow(new Object[]{nodePath});
                    }
                }

            }
        });

        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                montoyaApi.persistence().extensionData().deleteString("node_modules");
                tableModel.setRowCount(0);
            }
        });

        panel.add(scrollTable, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    public MontoyaApi getMontoyaApi() {
        return montoyaApi;
    }

    public void setMontoyaApi(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
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

    public ArrayList<Source> getListPureLibray() {
        return listPureLibray;
    }

    public void setListPureLibray(ArrayList<Source> listPureLibray) {
        this.listPureLibray = listPureLibray;
    }

    public DefaultTableModel getNodejsTableModel() {
        return nodejsTableModel;
    }

    public void setNodejsTableModel(DefaultTableModel nodejsTableModel) {
        this.nodejsTableModel = nodejsTableModel;
    }

    public DefaultTableModel getVariableTableModel() {
        return variableTableModel;
    }

    public void setVariableTableModel(DefaultTableModel variableTableModel) {
        this.variableTableModel = variableTableModel;
    }

    public void setBelowSplitPaneVertizontalModified(JSplitPane belowSplitPaneVertizontalModified) {
        this.belowSplitPaneVertizontalModified = belowSplitPaneVertizontalModified;
    }

    public JSplitPane getMainSlitPaneHorizontal() {
        return mainSlitPaneHorizontal;
    }

    public void setMainSlitPaneHorizontal(JSplitPane mainSlitPaneHorizontal) {
        this.mainSlitPaneHorizontal = mainSlitPaneHorizontal;
    }

    public JSplitPane getAboveSplitPaneVertizontal() {
        return aboveSplitPaneVertizontal;
    }

    public void setAboveSplitPaneVertizontal(JSplitPane aboveSplitPaneVertizontal) {
        this.aboveSplitPaneVertizontal = aboveSplitPaneVertizontal;
    }

    public JPanel getTableRequestPanel() {
        return tableRequestPanel;
    }

    public void setTableRequestPanel(JPanel tableRequestPanel) {
        this.tableRequestPanel = tableRequestPanel;
    }

    public JPanel getExtensionPanel() {
        return extensionPanel;
    }

    public void setExtensionPanel(JPanel extensionPanel) {
        this.extensionPanel = extensionPanel;
    }

    public JPanel getExtensionButtonPanel() {
        return extensionButtonPanel;
    }

    public void setExtensionButtonPanel(JPanel extensionButtonPanel) {
        this.extensionButtonPanel = extensionButtonPanel;
    }

    public JCheckBox getSaveLogButton() {
        return saveLogButton;
    }

    public void setSaveLogButton(JCheckBox saveLogButton) {
        this.saveLogButton = saveLogButton;
    }

    public JCheckBox getRunPrescriptButton() {
        return runPrescriptButton;
    }

    public void setRunPrescriptButton(JCheckBox runPrescriptButton) {
        this.runPrescriptButton = runPrescriptButton;
    }

    public JCheckBox getRunPostscriptButton() {
        return runPostscriptButton;
    }

    public void setRunPostscriptButton(JCheckBox runPostscriptButton) {
        this.runPostscriptButton = runPostscriptButton;
    }

    public JTabbedPane getExtensionTabbedPane() {
        return extensionTabbedPane;
    }

    public void setExtensionTabbedPane(JTabbedPane extensionTabbedPane) {
        this.extensionTabbedPane = extensionTabbedPane;
    }

    public JPanel getPrescriptPanel() {
        return prescriptPanel;
    }

    public void setPrescriptPanel(JPanel prescriptPanel) {
        this.prescriptPanel = prescriptPanel;
    }

//    public RawEditor getPrescriptTextArea() {
//        return prescriptTextArea;
//    }
//
//    public void setPrescriptTextArea(RawEditor prescriptTextArea) {
//        this.prescriptTextArea = prescriptTextArea;
//    }
//
//    public RawEditor getPrescriptRegexTextArea() {
//        return prescriptRegexTextArea;
//    }
//
//    public void setPrescriptRegexTextArea(RawEditor prescriptRegexTextArea) {
//        this.prescriptRegexTextArea = prescriptRegexTextArea;
//    }


    public RSyntaxTextArea getPrescriptTextArea() {
        return prescriptTextArea;
    }

    public void setPrescriptTextArea(RSyntaxTextArea prescriptTextArea) {
        this.prescriptTextArea = prescriptTextArea;
    }

    public RSyntaxTextArea getPrescriptRegexTextArea() {
        return prescriptRegexTextArea;
    }

    public void setPrescriptRegexTextArea(RSyntaxTextArea prescriptRegexTextArea) {
        this.prescriptRegexTextArea = prescriptRegexTextArea;
    }

    public void setPostscriptTextArea(RSyntaxTextArea postscriptTextArea) {
        this.postscriptTextArea = postscriptTextArea;
    }

    public void setPostscriptRegexTextArea(RSyntaxTextArea postscriptRegexTextArea) {
        this.postscriptRegexTextArea = postscriptRegexTextArea;
    }

    public JCheckBox getPrescriptIsNotModifyRequestFromProxyCheckbox() {
        return prescriptIsNotModifyRequestFromProxyCheckbox;
    }

    public void setPrescriptIsNotModifyRequestFromProxyCheckbox(JCheckBox prescriptIsNotModifyRequestFromProxyCheckbox) {
        this.prescriptIsNotModifyRequestFromProxyCheckbox = prescriptIsNotModifyRequestFromProxyCheckbox;
    }

    public JCheckBox getPrescriptIsInScopeCheckBox() {
        return prescriptIsInScopeCheckBox;
    }

    public void setPrescriptIsInScopeCheckBox(JCheckBox prescriptIsInScopeCheckBox) {
        this.prescriptIsInScopeCheckBox = prescriptIsInScopeCheckBox;
    }

    public JCheckBox getPrescriptIsMatchRegexCheckBox() {
        return prescriptIsMatchRegexCheckBox;
    }

    public void setPrescriptIsMatchRegexCheckBox(JCheckBox prescriptIsMatchRegexCheckBox) {
        this.prescriptIsMatchRegexCheckBox = prescriptIsMatchRegexCheckBox;
    }

    public JPanel getPostscriptPanel() {
        return postscriptPanel;
    }

    public void setPostscriptPanel(JPanel postscriptPanel) {
        this.postscriptPanel = postscriptPanel;
    }

//    public RawEditor getPostscriptTextArea() {
//        return postscriptTextArea;
//    }
//
//    public void setPostscriptTextArea(RawEditor postscriptTextArea) {
//        this.postscriptTextArea = postscriptTextArea;
//    }
//
//    public RawEditor getPostscriptRegexTextArea() {
//        return postscriptRegexTextArea;
//    }
//
//    public void setPostscriptRegexTextArea(RawEditor postscriptRegexTextArea) {
//        this.postscriptRegexTextArea = postscriptRegexTextArea;
//    }


    public RSyntaxTextArea getPostscriptTextArea() {
        return postscriptTextArea;
    }

    public RSyntaxTextArea getPostscriptRegexTextArea() {
        return postscriptRegexTextArea;
    }

    public JCheckBox getPostscriptIsNotModifyResponseFromProxyCheckBox() {
        return postscriptIsNotModifyResponseFromProxyCheckBox;
    }

    public void setPostscriptIsNotModifyResponseFromProxyCheckBox(JCheckBox postscriptIsNotModifyResponseFromProxyCheckBox) {
        this.postscriptIsNotModifyResponseFromProxyCheckBox = postscriptIsNotModifyResponseFromProxyCheckBox;
    }

    public JCheckBox getPostscriptIsMatchRegexCheckBox() {
        return postscriptIsMatchRegexCheckBox;
    }

    public void setPostscriptIsMatchRegexCheckBox(JCheckBox postscriptIsMatchRegexCheckBox) {
        this.postscriptIsMatchRegexCheckBox = postscriptIsMatchRegexCheckBox;
    }

    public JPanel getVariablePanel() {
        return variablePanel;
    }

    public void setVariablePanel(JPanel variablePanel) {
        this.variablePanel = variablePanel;
    }

    public JPanel getPureLibraryPanel() {
        return pureLibraryPanel;
    }

    public void setPureLibraryPanel(JPanel pureLibraryPanel) {
        this.pureLibraryPanel = pureLibraryPanel;
    }

    public JPanel getNodejsLibraryPanel() {
        return nodejsLibraryPanel;
    }

    public void setNodejsLibraryPanel(JPanel nodejsLibraryPanel) {
        this.nodejsLibraryPanel = nodejsLibraryPanel;
    }

    public DefaultTableModel getPureTableModel() {
        return pureTableModel;
    }

    public void setPureTableModel(DefaultTableModel pureTableModel) {
        this.pureTableModel = pureTableModel;
    }

    public JTabbedPane getBelowTabbedPane() {
        return belowTabbedPane;
    }

    public void setBelowTabbedPane(JTabbedPane belowTabbedPane) {
        this.belowTabbedPane = belowTabbedPane;
    }

    public JSplitPane getBelowSplitPaneVertizontalOriginal() {
        return belowSplitPaneVertizontalOriginal;
    }

    public void setBelowSplitPaneVertizontalOriginal(JSplitPane belowSplitPaneVertizontalOriginal) {
        this.belowSplitPaneVertizontalOriginal = belowSplitPaneVertizontalOriginal;
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

    public JSplitPane getBelowSplitPaneVertizontalModified() {
        return this.belowSplitPaneVertizontalModified;
    }

    public void setGetBelowSplitPaneVertizontalModified(JSplitPane getBelowSplitPaneVertizontalModified) {
        this.belowSplitPaneVertizontalModified = getBelowSplitPaneVertizontalModified;
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
    public JButton getRemoveLogButton() {
        return removeLogButton;
    }

    public void setRemoveLogButton(JButton removeLogButton) {
        this.removeLogButton = removeLogButton;
    }

    public HashMap<String, String> getLogRowHashMap() {
        return logRowHashMap;
    }

    public void setLogRowHashMap(HashMap<String, String> logRowHashMap) {
        this.logRowHashMap = logRowHashMap;
    }

    public JPanel getConfigurationPanel() {
        return configurationPanel;
    }

    public void setConfigurationPanel(JPanel configurationPanel) {
        this.configurationPanel = configurationPanel;
    }

    public JRadioButton getRunWithGraaljs() {
        return runWithGraaljs;
    }

    public void setRunWithGraaljs(JRadioButton runWithGraaljs) {
        this.runWithGraaljs = runWithGraaljs;
    }

    public JRadioButton getRunWithJavet() {
        return runWithJavet;
    }

    public void setRunWithJavet(JRadioButton runWithJavet) {
        this.runWithJavet = runWithJavet;
    }
}
