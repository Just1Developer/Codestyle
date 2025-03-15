package net.justonedev.codestyle.checks;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class VisibilityTableModel extends AbstractTableModel {
    private final String[] columns = {"Class", "Method", "From", "To"};
    private List<VisibilityResult> data = new ArrayList<>();

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        VisibilityResult row = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return row.className();
            case 1: return row.methodName();
            case 2: return row.oldVisibility();
            case 3: return row.newVisibility();
            default: return "";
        }
    }

    public void setResults(List<VisibilityResult> newData) {
        this.data = newData;
        fireTableDataChanged();
    }
}
