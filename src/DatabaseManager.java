import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connection;
    private File currentDatabase;

    public boolean createDatabase(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            Connection conn = DriverManager.getConnection(url);
            conn.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean connect(File dbFile) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            currentDatabase = dbFile;

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public File getCurrentDatabase() {
        return currentDatabase;
    }

    public List<String> getTables() {
        List<String> tables = new ArrayList<>();

        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, null, new String[]{"TABLE"});

            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    public boolean executeUpdate(String sql) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createTable(String tableName, List<String> columns) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName).append(" (");
        sql.append("id INTEGER PRIMARY KEY AUTOINCREMENT");

        for (String column : columns) {
            sql.append(", ").append(column);
        }

        sql.append(")");

        return executeUpdate(sql.toString());
    }

    public boolean insertRow(String tableName, List<String> columns, List<String> values) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");

        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            if (i < columns.size() - 1) sql.append(", ");
        }

        sql.append(") VALUES (");

        for (int i = 0; i < values.size(); i++) {
            sql.append("'").append(values.get(i).replace("'", "''")).append("'");
            if (i < values.size() - 1) sql.append(", ");
        }

        sql.append(")");

        return executeUpdate(sql.toString());
    }

    public boolean importSql(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");

                if (line.trim().endsWith(";")) {
                    executeUpdate(sql.toString());
                    sql = new StringBuilder();
                }
            }

            reader.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportSql(File file) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            List<String> tables = getTables();

            for (String table : tables) {
                ResultSet rs = executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='" + table + "'");

                if (rs != null && rs.next()) {
                    writer.println(rs.getString("sql") + ";");
                    writer.println();

                    ResultSet data = executeQuery("SELECT * FROM " + table);
                    if (data != null) {
                        ResultSetMetaData metaData = data.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        while (data.next()) {
                            StringBuilder insert = new StringBuilder("INSERT INTO " + table + " VALUES (");

                            for (int i = 1; i <= columnCount; i++) {
                                String value = data.getString(i);
                                if (value == null) {
                                    insert.append("NULL");
                                } else {
                                    insert.append("'").append(value.replace("'", "''")).append("'");
                                }

                                if (i < columnCount) insert.append(", ");
                            }

                            insert.append(");");
                            writer.println(insert);
                        }

                        data.close();
                    }

                    writer.println();
                    rs.close();
                }
            }

            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
