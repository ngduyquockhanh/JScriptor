package org.JScriptor.Logs;

import javax.swing.table.DefaultTableModel;

public class LogTableModel extends DefaultTableModel {
    public LogTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public LogTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // No cell can be edited
        return false;
    }
}
