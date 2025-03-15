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
        return switch (columnIndex) {
            case 0 -> row.getClassName();
            case 1 -> row.getMethodName();
            case 2 -> {
                Visibility oldVis = row.oldVisibility();
                yield (oldVis != null) ? oldVis.name() : "";
            }
            case 3 -> {
                Visibility newVis = row.newVisibility();
                yield (newVis != null) ? newVis.name() : "";
            }
            default -> "";
        };
    }

    public void setResults(List<VisibilityResult> newData) {
        this.data = newData;
        fireTableDataChanged();
    }

    public VisibilityResult getResultAt(int rowIndex) {
        return data.get(rowIndex);
    }
}
