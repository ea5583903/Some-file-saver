import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.function.Consumer;

public class FileHandler {

    public static Component openSqlFile(File file, JTabbedPane contentPane, Consumer<String> logger) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();

            JTextArea textArea = new JTextArea(content.toString());
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            textArea.setLineWrap(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            contentPane.addTab(file.getName(), scrollPane);
            contentPane.setSelectedComponent(scrollPane);

            logger.accept("Opened SQL file: " + file.getName());
            return scrollPane;
        } catch (IOException e) {
            logger.accept("ERROR: Failed to open SQL file - " + e.getMessage());
            return null;
        }
    }

    public static Component openTextFile(File file, JTabbedPane contentPane, Consumer<String> logger) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();

            JTextArea textArea = new JTextArea(content.toString());
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            contentPane.addTab(file.getName(), scrollPane);
            contentPane.setSelectedComponent(scrollPane);

            logger.accept("Opened text file: " + file.getName());
            return scrollPane;
        } catch (IOException e) {
            logger.accept("ERROR: Failed to open file - " + e.getMessage());
            return null;
        }
    }

    public static void saveToPdf(File file, JTabbedPane contentPane, Consumer<String> logger) {
        try {
            Component selected = contentPane.getSelectedComponent();

            if (selected instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) selected;
                JViewport viewport = scrollPane.getViewport();
                Component view = viewport.getView();

                if (view instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) view;
                    String content = textArea.getText();

                    PrintWriter writer = new PrintWriter(new FileWriter(file));
                    writer.println("PDF Export");
                    writer.println("Generated: " + new java.util.Date());
                    writer.println("---");
                    writer.println();
                    writer.println(content);
                    writer.close();

                    logger.accept("Saved to PDF: " + file.getName() + " (basic format)");
                }
            } else {
                logger.accept("ERROR: Cannot save current tab to PDF");
            }
        } catch (IOException e) {
            logger.accept("ERROR: Failed to save PDF - " + e.getMessage());
        }
    }

    public static void saveDocument(File file, JTabbedPane contentPane, Consumer<String> logger) {
        try {
            Component selected = contentPane.getSelectedComponent();

            if (selected instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) selected;
                JViewport viewport = scrollPane.getViewport();
                Component view = viewport.getView();

                if (view instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) view;
                    String content = textArea.getText();

                    PrintWriter writer = new PrintWriter(new FileWriter(file));
                    writer.print(content);
                    writer.close();

                    logger.accept("Saved document: " + file.getName());
                }
            } else {
                logger.accept("ERROR: Cannot save current tab as document");
            }
        } catch (IOException e) {
            logger.accept("ERROR: Failed to save document - " + e.getMessage());
        }
    }

    public static void saveOdbFile(File file, Object data, Consumer<String> logger) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(data);
            oos.close();

            logger.accept("Saved ODB file: " + file.getName());
        } catch (IOException e) {
            logger.accept("ERROR: Failed to save ODB file - " + e.getMessage());
        }
    }

    public static Object loadOdbFile(File file, Consumer<String> logger) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Object data = ois.readObject();
            ois.close();

            logger.accept("Loaded ODB file: " + file.getName());
            return data;
        } catch (Exception e) {
            logger.accept("ERROR: Failed to load ODB file - " + e.getMessage());
            return null;
        }
    }

    public static void saveSqlFile(File file, String sqlContent, Consumer<String> logger) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.print(sqlContent);
            writer.close();

            logger.accept("Saved SQL file: " + file.getName());
        } catch (IOException e) {
            logger.accept("ERROR: Failed to save SQL file - " + e.getMessage());
        }
    }
}
