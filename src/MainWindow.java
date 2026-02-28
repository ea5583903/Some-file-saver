import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {
    private JTree fileTree;
    private JTextArea monitorArea;
    private JTabbedPane contentPane;
    private DatabaseManager dbManager;
    private List<File> watchedFiles;
    private Map<Component, File> tabFileMap;

    public MainWindow() {
        setTitle("Database & File Manager");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        dbManager = new DatabaseManager();
        watchedFiles = new ArrayList<>();
        tabFileMap = new HashMap<>();

        initComponents();
        setupMenuBar();
        startFileMonitor();
    }

    private void initComponents() {
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(300);

        JPanel leftPanel = createFileFinderPanel();
        mainSplit.setLeftComponent(leftPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());

        contentPane = new JTabbedPane();
        rightPanel.add(contentPane, BorderLayout.CENTER);

        monitorArea = new JTextArea();
        monitorArea.setEditable(false);
        monitorArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane monitorScroll = new JScrollPane(monitorArea);
        monitorScroll.setPreferredSize(new Dimension(0, 150));
        rightPanel.add(monitorScroll, BorderLayout.SOUTH);

        mainSplit.setRightComponent(rightPanel);
        add(mainSplit);

        logMonitor("Database & File Manager started");
    }

    private JPanel createFileFinderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("File Finder"));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Workspace");
        fileTree = new JTree(root);
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        fileTree.getLastSelectedPathComponent();
                    if (node != null && node.getUserObject() instanceof File) {
                        File file = (File) node.getUserObject();
                        openFile(file);
                    }
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(fileTree);
        panel.add(treeScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addFolderBtn = new JButton("Add Folder");
        addFolderBtn.addActionListener(e -> addFolder());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshFileTree());

        buttonPanel.add(addFolderBtn);
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newFileItem = new JMenuItem("New File");
        newFileItem.addActionListener(e -> createNewFile());
        JMenuItem newDbItem = new JMenuItem("New Database");
        newDbItem.addActionListener(e -> createNewDatabase());
        JMenuItem openItem = new JMenuItem("Open File");
        openItem.addActionListener(e -> openFileDialog());
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveCurrentFile());
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveAsNewFile());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        JMenuItem closeTabItem = new JMenuItem("Close Tab");
        closeTabItem.addActionListener(e -> closeCurrentTab());

        fileMenu.add(newFileItem);
        fileMenu.add(newDbItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(closeTabItem);
        fileMenu.add(exitItem);

        JMenu databaseMenu = new JMenu("Database");
        JMenuItem createTableItem = new JMenuItem("Create Table");
        createTableItem.addActionListener(e -> createTable());
        JMenuItem importSqlItem = new JMenuItem("Import SQL");
        importSqlItem.addActionListener(e -> importSqlFile());
        JMenuItem exportSqlItem = new JMenuItem("Export SQL");
        exportSqlItem.addActionListener(e -> exportSqlFile());

        databaseMenu.add(createTableItem);
        databaseMenu.add(importSqlItem);
        databaseMenu.add(exportSqlItem);

        JMenu documentsMenu = new JMenu("Documents");
        JMenuItem savePdfItem = new JMenuItem("Save as PDF");
        savePdfItem.addActionListener(e -> saveToPdf());
        JMenuItem saveDocItem = new JMenuItem("Save Document");
        saveDocItem.addActionListener(e -> saveDocument());

        documentsMenu.add(savePdfItem);
        documentsMenu.add(saveDocItem);

        menuBar.add(fileMenu);
        menuBar.add(databaseMenu);
        menuBar.add(documentsMenu);

        setJMenuBar(menuBar);
    }

    private void addFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File folder = chooser.getSelectedFile();
            addFolderToTree(folder);
            logMonitor("Added folder: " + folder.getAbsolutePath());
        }
    }

    private void addFolderToTree(File folder) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) fileTree.getModel().getRoot();
        DefaultMutableTreeNode folderNode = createTreeNode(folder);
        root.add(folderNode);
        ((DefaultTreeModel) fileTree.getModel()).reload();
        expandTree();
    }

    private DefaultMutableTreeNode createTreeNode(File file) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file) {
            @Override
            public String toString() {
                return ((File) getUserObject()).getName();
            }
        };

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (isSupported(child)) {
                        node.add(createTreeNode(child));
                    }
                }
            }
        }

        return node;
    }

    private boolean isSupported(File file) {
        if (file.isDirectory()) return true;
        String name = file.getName().toLowerCase();

        // Exclude image files
        String[] imageExtensions = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico",
                                   ".svg", ".webp", ".tiff", ".tif"};
        for (String ext : imageExtensions) {
            if (name.endsWith(ext)) {
                return false;
            }
        }

        // Accept all other files
        return true;
    }

    private void refreshFileTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) fileTree.getModel().getRoot();
        root.removeAllChildren();
        ((DefaultTreeModel) fileTree.getModel()).reload();
        logMonitor("File tree refreshed");
    }

    private void expandTree() {
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }

    private void openFile(File file) {
        logMonitor("Opening file: " + file.getName());

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".db") || fileName.endsWith(".sqlite")) {
            openDatabase(file);
        } else if (fileName.endsWith(".odb")) {
            openOdbFile(file);
        } else if (fileName.endsWith(".sql")) {
            openSqlFile(file);
        } else if (fileName.endsWith(".pdf")) {
            openPdfFile(file);
        } else {
            openTextFile(file);
        }
    }

    private void openFileDialog() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }

    private void createNewFile() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        contentPane.addTab("Untitled", scrollPane);
        contentPane.setSelectedComponent(scrollPane);

        logMonitor("Created new file");
    }

    private void createNewDatabase() {
        String dbName = JOptionPane.showInputDialog(this, "Enter database name:");
        if (dbName != null && !dbName.trim().isEmpty()) {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(dbName + ".db"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File dbFile = chooser.getSelectedFile();
                if (dbManager.createDatabase(dbFile)) {
                    logMonitor("Created database: " + dbFile.getAbsolutePath());
                    openDatabase(dbFile);
                } else {
                    logMonitor("ERROR: Failed to create database");
                }
            }
        }
    }

    private void openDatabase(File file) {
        if (dbManager.connect(file)) {
            TableViewer viewer = new TableViewer(dbManager, file);
            contentPane.addTab(file.getName(), viewer);
            contentPane.setSelectedComponent(viewer);
            logMonitor("Opened database: " + file.getName());
        } else {
            logMonitor("ERROR: Failed to open database");
        }
    }

    private void openOdbFile(File file) {
        JTextArea textArea = new JTextArea();
        textArea.setText("ODB File: " + file.getName() + "\n\n");
        textArea.append("ODB (OpenDocument Database) support\n");
        textArea.append("File location: " + file.getAbsolutePath());

        JScrollPane scrollPane = new JScrollPane(textArea);
        contentPane.addTab(file.getName(), scrollPane);
        tabFileMap.put(scrollPane, file);
        logMonitor("Opened ODB file: " + file.getName());
    }

    private void openSqlFile(File file) {
        Component component = FileHandler.openSqlFile(file, contentPane, this::logMonitor);
        if (component != null) {
            tabFileMap.put(component, file);
        }
    }

    private void openPdfFile(File file) {
        JTextArea textArea = new JTextArea();
        textArea.setText("PDF File: " + file.getName() + "\n\n");
        textArea.append("File location: " + file.getAbsolutePath());

        contentPane.addTab(file.getName(), new JScrollPane(textArea));
        logMonitor("Opened PDF file: " + file.getName());
    }

    private void openTextFile(File file) {
        Component component = FileHandler.openTextFile(file, contentPane, this::logMonitor);
        if (component != null) {
            tabFileMap.put(component, file);
        }
    }

    private void createTable() {
        if (contentPane.getSelectedComponent() instanceof TableViewer) {
            TableViewer viewer = (TableViewer) contentPane.getSelectedComponent();
            viewer.showCreateTableDialog();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a database first");
        }
    }

    private void importSqlFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (dbManager.importSql(file)) {
                logMonitor("Imported SQL file: " + file.getName());
            } else {
                logMonitor("ERROR: Failed to import SQL file");
            }
        }
    }

    private void exportSqlFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("export.sql"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (dbManager.exportSql(file)) {
                logMonitor("Exported SQL to: " + file.getName());
            } else {
                logMonitor("ERROR: Failed to export SQL");
            }
        }
    }

    private void saveToPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("document.pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            FileHandler.saveToPdf(file, contentPane, this::logMonitor);
        }
    }

    private void saveDocument() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            FileHandler.saveDocument(file, contentPane, this::logMonitor);
        }
    }

    private void saveCurrentFile() {
        Component selected = contentPane.getSelectedComponent();

        if (selected instanceof TableViewer) {
            TableViewer viewer = (TableViewer) selected;
            viewer.saveChanges();
            logMonitor("Saved database changes");
        } else if (tabFileMap.containsKey(selected)) {
            File file = tabFileMap.get(selected);
            saveComponentToFile(selected, file);
        } else {
            saveAsNewFile();
        }
    }

    private void saveComponentToFile(Component component, File file) {
        if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) component;
            JViewport viewport = scrollPane.getViewport();
            Component view = viewport.getView();

            if (view instanceof JTextArea) {
                JTextArea textArea = (JTextArea) view;
                String content = textArea.getText();

                try {
                    java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file));
                    writer.print(content);
                    writer.close();
                    logMonitor("Saved file: " + file.getName());
                } catch (java.io.IOException e) {
                    logMonitor("ERROR: Failed to save file - " + e.getMessage());
                }
            }
        }
    }

    private void saveAsNewFile() {
        Component selected = contentPane.getSelectedComponent();
        JFileChooser chooser = new JFileChooser();

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveComponentToFile(selected, file);
            tabFileMap.put(selected, file);

            int index = contentPane.getSelectedIndex();
            if (index >= 0) {
                contentPane.setTitleAt(index, file.getName());
            }
        }
    }

    private void closeCurrentTab() {
        int index = contentPane.getSelectedIndex();
        if (index >= 0) {
            Component component = contentPane.getComponentAt(index);
            contentPane.removeTabAt(index);
            tabFileMap.remove(component);
            logMonitor("Closed tab");
        }
    }

    private void startFileMonitor() {
        Timer timer = new Timer(5000, e -> {
            for (File file : watchedFiles) {
                if (file.exists()) {
                    logMonitor("Monitoring: " + file.getName() + " - Last modified: " +
                        new java.util.Date(file.lastModified()));
                }
            }
        });
        timer.start();
    }

    private void logMonitor(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        monitorArea.append("[" + timestamp + "] " + message + "\n");
        monitorArea.setCaretPosition(monitorArea.getDocument().getLength());
    }
}
