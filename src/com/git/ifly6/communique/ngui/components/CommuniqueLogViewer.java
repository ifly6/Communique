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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.LogRecord;

public class CommuniqueLogViewer extends JSplitPane {

    private final CommuniqueLogTableModel tableModel;
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private JTable table;
    private JLabel source;
    private JLabel date;

    public CommuniqueLogViewer() {
        super(JSplitPane.VERTICAL_SPLIT);
        this.setContinuousLayout(true);
        this.setBorder(CommuniqueFactory.createBorder(0));
        this.setDividerSize(1);

        // initialise south components
        JPanel southPane = new JPanel();
        southPane.setMinimumSize(new Dimension(300, 100));
        southPane.setLayout(new BorderLayout(5, 5));
        CommuniqueScrollableTextArea southScroll = new CommuniqueScrollableTextArea(
                CommuniqueFactory.createArea("", null)
        );
        southScroll.setFontSize(11);
        southScroll.setEditable(false);

        JPanel southHeader = new JPanel();
        southHeader.setLayout(new BorderLayout(5, 5));
        southHeader.setBorder(new EmptyBorder(10, 0, 5, 0));
        source = new JLabel("");
        source.setVerticalAlignment(JLabel.TOP);
        date = new JLabel("");
        date.setVerticalAlignment(JLabel.BOTTOM);

        // initialise north components
        tableModel = new CommuniqueLogTableModel();
        table = new JTable(tableModel);
        table.setBorder(CommuniqueFactory.createBorder(5));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        Font monospaced = new Font(Font.MONOSPACED, Font.PLAIN, 11);
        table.setFont(monospaced); // start viewing section
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            switch (i) {
                case 0:
                    column.setMinWidth(table.getFontMetrics(table.getFont()).stringWidth("2024-01-01 "));
                    int width = table.getFontMetrics(table.getFont()).stringWidth("2024-01-01 00:00:00 ");
                    column.setMaxWidth(width + 1);
                    column.setPreferredWidth(width + 1);
                    break;
                case 1:
                    column.setMinWidth(125);
                    column.setPreferredWidth(160);
                    break;
                case 2:
                    column.setMinWidth(100);
                    break;
            }
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            LogRecord record = tableModel.getRecord(table.getSelectedRow());
            EventQueue.invokeLater(() -> {
                // print the message and stack trace
                southScroll.setText(record.getMessage());
                if (record.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    record.getThrown().printStackTrace(new PrintWriter(sw, true));
                    southScroll.appendLine(sw.toString());
                }

                // update the other ui elements
                source.setText(String.format("<html>%s<br />%s</html>",
                        record.getSourceClassName(),
                        record.getSourceMethodName()
                ));
                date.setText(formatter.format(record.getMillis()));
            });
        });

        JScrollPane northScroll = new JScrollPane(table);
        northScroll.setMinimumSize(new Dimension(300, 200));

        table.getModel().addTableModelListener(e -> {
            // table starts with nothing selected; select first item when it populates in
            if (table.getSelectedRow() == -1 && table.getModel().getRowCount() > 0)
                EventQueue.invokeLater(() -> table.setRowSelectionInterval(0, 0));  // need to avoid race condition

            EventQueue.invokeLater(() -> {
                JScrollBar vs = northScroll.getVerticalScrollBar(); // scroll to bottom
                vs.setValue(Math.max(vs.getMinimum(), vs.getMaximum()));
            });
        });

        // place the components about
        southHeader.add(source, BorderLayout.WEST);
        southHeader.add(date, BorderLayout.EAST);
        southPane.add(southHeader, BorderLayout.NORTH);
        southPane.add(southScroll, BorderLayout.CENTER);

        this.add(northScroll, JSplitPane.TOP);
        this.add(southPane, JSplitPane.BOTTOM);
        EventQueue.invokeLater(() -> this.setDividerLocation(0.75));
    }

    public CommuniqueLogTableModel getModel() {
        return tableModel;
    }

}
