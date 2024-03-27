/*
 * Copyright (c) 2024 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique.ngui.components;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

public class CommuniqueLogTableModel extends AbstractTableModel {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String[] columnNames = new String[] {"Date", "Source", "Message"};
    private List<LogRecord> records = new ArrayList<>();

    public CommuniqueLogTableModel() {
    }

    public void appendRecord(LogRecord r) {
        records.add(r);
        fireTableRowsInserted(records.size() - 1, records.size() - 1);
    }

    @Override
    public String getColumnName(int i) {
        return (i < columnNames.length) ? columnNames[i] : "";
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogRecord record = records.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return formatter.format(record.getMillis());
            case 1:
                return record.getSourceClassName() + " " + record.getSourceMethodName();
            case 2:
                return record.getMessage();
        }

        return "";
    }

    public LogRecord getRecord(int selectedRow) {
        return records.get(selectedRow);
    }
}
