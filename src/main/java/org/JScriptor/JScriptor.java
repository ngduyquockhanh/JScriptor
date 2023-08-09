package org.JScriptor;
import burp.api.montoya.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class JScriptor implements  BurpExtension{
    private MontoyaApi montoyaApi;
    private JTextArea prescript = new JTextArea();
    private JTextArea postscript = new JTextArea();
    private JToggleButton runPrescript = new JToggleButton("Run Pre-script");
    private JToggleButton runPostscript = new JToggleButton("Run Post-script");
    private  DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"File Path"}, 0);

    @Override
    public void initialize(MontoyaApi api) {
        this.montoyaApi = api;
        api.extension().setName("JScriptor");

        JPanel JScriptorPanel = new JPanel();
        JScriptorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.add("Pre-script", createTabPanel(this.prescript, this.runPrescript));
        tabbedPane.add("Post-script", createTabPanel(this.postscript, this.runPostscript));
        tabbedPane.add("Setting Libary", createSettingTabPanel(this.tableModel));
        JScriptorPanel.add(tabbedPane);

        api.userInterface().registerSuiteTab("JScriptor", JScriptorPanel);

        HttpHandlerWithScript httpHandlerWithScript = new HttpHandlerWithScript(api, prescript, postscript, runPrescript, runPostscript, tableModel);
        api.http().registerHttpHandler(httpHandlerWithScript);
    }

    private static JPanel createTabPanel(JTextArea script, JToggleButton runButton) {
        JPanel panel = new JPanel(new BorderLayout());
        script.setEditable(true);
        script.setLineWrap(true);
        script.setWrapStyleWord(true);
        script.setRows(30);
        script.setColumns(100);

        JScrollPane scrollPane = new JScrollPane(script);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(runButton, BorderLayout.SOUTH);

        return panel;
    }

    private static JPanel createSettingTabPanel(DefaultTableModel tableModel) {
        JPanel panel = new JPanel(new BorderLayout());
        JButton chooseFileButton = new JButton("Add File");
        JButton removeSelectedButton = new JButton("Remove Selected");
        JButton clearAllButton = new JButton("Clear All");

        JTable table = new JTable(tableModel);
        JScrollPane crollTable = new JScrollPane(table);
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
                        tableModel.addRow(new String[]{filePath});
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
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Clear all rows from the table
                tableModel.setRowCount(0);
            }
        });

        panel.add(crollTable, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }
}
