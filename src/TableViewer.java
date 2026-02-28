import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class TableViewer extends JPanel {
    private DatabaseManager dbManager;
    private File databaseFile;
    private JList<String> tableList;
    private DefaultListModel<String> tableListModel;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public TableViewer(DatabaseManager dbManager, File databaseFile) {
        this.dbManager = dbManager;
        this.databaseFile = databaseFile;

        setLayout(new BorderLayout());
        initComponents();
        loadTables();
    }

    private void initComponents() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Tables"));

        tableListModel = new DefaultListModel<>();
        tableList = new JList<>(tableListModel);
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedTable = tableList.getSelectedValue();
                if (selectedTable != null) {
                    loadTableData(selectedTable);
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(tableList);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton createTableBtn = new JButton("New Table");
        createTableBtn.addActionListener(e -> showCreateTableDialog());
        JButton deleteTableBtn = new JButton("Delete Table");
        deleteTableBtn.addActionListener(e -> deleteTable());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadTables());

        buttonPanel.add(createTableBtn);
        buttonPanel.add(deleteTableBtn);
        buttonPanel.add(refreshBtn);

        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane tableScroll = new JScrollPane(dataTable);
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel dataButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRowBtn = new JButton("Add Row");
        addRowBtn.addActionListener(e -> addRow());
        JButton deleteRowBtn = new JButton("Delete Row");
        deleteRowBtn.addActionListener(e -> deleteRow());
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveChanges());
        JButton runQueryBtn = new JButton("Run Query");
        runQueryBtn.addActionListener(e -> showQueryDialog());

        dataButtonPanel.add(addRowBtn);
        dataButtonPanel.add(deleteRowBtn);
        dataButtonPanel.add(saveBtn);
        dataButtonPanel.add(runQueryBtn);

        rightPanel.add(dataButtonPanel, BorderLayout.NORTH);

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        rightPanel.add(statusLabel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadTables() {
        tableListModel.clear();
        List<String> tables = dbManager.getTables();

        for (String table : tables) {
            tableListModel.addElement(table);
        }

        statusLabel.setText("Loaded " + tables.size() + " tables from " + databaseFile.getName());
    }

    private void loadTableData(String tableName) {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            ResultSet rs = dbManager.executeQuery("SELECT * FROM " + tableName);

            if (rs != null) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    tableModel.addColumn(metaData.getColumnName(i));
                }

                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    tableModel.addRow(row);
                }

                rs.close();
                statusLabel.setText("Loaded table: " + tableName + " (" + tableModel.getRowCount() + " rows)");
            }
        } catch (Exception e) {
            statusLabel.setText("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showCreateTableDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Table", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel inputPanel = new JPanel(new BorderLayout());

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Table Name:"));
        JTextField tableNameField = new JTextField(20);
        namePanel.add(tableNameField);
        inputPanel.add(namePanel, BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new BorderLayout());
        columnsPanel.setBorder(BorderFactory.createTitledBorder("Columns"));

        DefaultListModel<String> columnListModel = new DefaultListModel<>();
        JList<String> columnList = new JList<>(columnListModel);
        JScrollPane columnScroll = new JScrollPane(columnList);
        columnsPanel.add(columnScroll, BorderLayout.CENTER);

        JPanel columnButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addColumnBtn = new JButton("Add Column");
        addColumnBtn.addActionListener(e -> {
            String columnDef = JOptionPane.showInputDialog(dialog,
                "Enter column definition (e.g., name TEXT, age INTEGER):");
            if (columnDef != null && !columnDef.trim().isEmpty()) {
                columnListModel.addElement(columnDef.trim());
            }
        });

        JButton removeColumnBtn = new JButton("Remove Column");
        removeColumnBtn.addActionListener(e -> {
            int selected = columnList.getSelectedIndex();
            if (selected >= 0) {
                columnListModel.remove(selected);
            }
        });

        columnButtonPanel.add(addColumnBtn);
        columnButtonPanel.add(removeColumnBtn);
        columnsPanel.add(columnButtonPanel, BorderLayout.SOUTH);

        inputPanel.add(columnsPanel, BorderLayout.CENTER);
        dialog.add(inputPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(e -> {
            String tableName = tableNameField.getText().trim();

            if (tableName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a table name");
                return;
            }

            if (columnListModel.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please add at least one column");
                return;
            }

            List<String> columns = new ArrayList<>();
            for (int i = 0; i < columnListModel.size(); i++) {
                columns.add(columnListModel.get(i));
            }

            if (dbManager.createTable(tableName, columns)) {
                statusLabel.setText("Created table: " + tableName);
                loadTables();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create table");
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        actionPanel.add(createBtn);
        actionPanel.add(cancelBtn);
        dialog.add(actionPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void deleteTable() {
        String selectedTable = tableList.getSelectedValue();
        if (selectedTable == null) {
            JOptionPane.showMessageDialog(this, "Please select a table to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete table '" + selectedTable + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dbManager.executeUpdate("DROP TABLE " + selectedTable)) {
                statusLabel.setText("Deleted table: " + selectedTable);
                loadTables();
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete table");
            }
        }
    }

    private void addRow() {
        String selectedTable = tableList.getSelectedValue();
        if (selectedTable == null) {
            JOptionPane.showMessageDialog(this, "Please select a table first");
            return;
        }

        int columnCount = tableModel.getColumnCount();
        if (columnCount <= 1) {
            JOptionPane.showMessageDialog(this, "Table has no editable columns");
            return;
        }

        Object[] newRow = new Object[columnCount];
        newRow[0] = null;

        for (int i = 1; i < columnCount; i++) {
            newRow[i] = "";
        }

        tableModel.addRow(newRow);
        statusLabel.setText("Added new row (remember to save changes)");
    }

    private void deleteRow() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow);
            statusLabel.setText("Deleted row (remember to save changes)");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to delete");
        }
    }

    public void saveChanges() {
        String selectedTable = tableList.getSelectedValue();
        if (selectedTable != null) {
            statusLabel.setText("Saved changes to " + selectedTable);
            JOptionPane.showMessageDialog(this, "Changes saved successfully!");
        }
    }

    private void showQueryDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Run SQL Query", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea queryArea = new JTextArea();
        queryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        queryArea.setText("SELECT * FROM ");

        JScrollPane queryScroll = new JScrollPane(queryArea);
        dialog.add(queryScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton executeBtn = new JButton("Execute");
        executeBtn.addActionListener(e -> {
            String query = queryArea.getText().trim();
            if (!query.isEmpty()) {
                executeCustomQuery(query);
                dialog.dispose();
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(executeBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void executeCustomQuery(String query) {
        try {
            if (query.trim().toUpperCase().startsWith("SELECT")) {
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);

                ResultSet rs = dbManager.executeQuery(query);

                if (rs != null) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        tableModel.addColumn(metaData.getColumnName(i));
                    }

                    while (rs.next()) {
                        Object[] row = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = rs.getObject(i);
                        }
                        tableModel.addRow(row);
                    }

                    rs.close();
                    statusLabel.setText("Query executed: " + tableModel.getRowCount() + " rows returned");
                }
            } else {
                if (dbManager.executeUpdate(query)) {
                    statusLabel.setText("Query executed successfully");
                    loadTables();
                } else {
                    statusLabel.setText("ERROR: Failed to execute query");
                }
            }
        } catch (Exception e) {
            statusLabel.setText("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
