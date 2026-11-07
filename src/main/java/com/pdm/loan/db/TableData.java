package com.pdm.loan.db;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public record TableData(List<String> columns, List<Object[]> rows) {

    public List<Rec> recs() {
        List<Rec> list = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                m.put(columns.get(i), row[i]);
            }
            list.add(new Rec(m));
        }
        return list;
    }

    public Rec first() {
        List<Rec> r = recs();
        return r.isEmpty() ? null : r.get(0);
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public DefaultTableModel toTableModel() {
        DefaultTableModel model = new DefaultTableModel(columns.toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : rows) {
            model.addRow(row);
        }
        return model;
    }
}
