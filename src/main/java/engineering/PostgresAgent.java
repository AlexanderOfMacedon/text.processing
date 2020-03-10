package engineering;


import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.*;

public class PostgresAgent implements DatabaseAgent {
    private final Connection connection;

    public PostgresAgent(String databaseURL, Properties properties) throws SQLException {
        this.connection = DriverManager.getConnection(
                databaseURL, properties);
    }

    @Override
    public ResultSet select(String tableName, JSONObject selectOptions) throws SQLException {
        String sqlQuery = "SELECT * FROM " + tableName;
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(sqlQuery);
    }

    @Override
    public void insert(String tableName, JSONObject row) throws SQLException {
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("INSERT INTO " + tableName + " (");
        Object[] keys = row.keySet().toArray();
        String prefix = "";
        for (Object column : keys) {
            sqlQuery.append(prefix);
            prefix = ",";
            sqlQuery.append(column);
        }
        sqlQuery.append(") VALUES (");
        prefix = "";
        for (Object column : keys) {
            sqlQuery.append(prefix);
            prefix = ",";
            sqlQuery.append("?");
        }
        sqlQuery.append(")");
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery.toString());
        for (int i = 0; i < keys.length; i++) {

            preparedStatement.setObject(i + 1, row.get((String) keys[i]));
        }
        preparedStatement.execute();
    }

    @Override
    public void createTable(String tableName, JSONObject columnsOptions) throws SQLException {
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("CREATE TABLE " + tableName + " (");
        for (Object column : columnsOptions.keySet()) {
            sqlQuery.append(column + " " + columnsOptions.getString((String) column) + ",");
        }
        sqlQuery.append("id BIGSERIAL)");
        connection.prepareStatement(sqlQuery.toString()).execute();
    }

    @Override
    public void deleteTable(String tableName) throws SQLException {
        connection.prepareStatement("DROP TABLE IF EXISTS " + tableName).execute();
    }

    @Override
    public ArrayList<String> columns(String tableName) throws SQLException, UnsupportedEncodingException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getColumns(null, null, tableName, null);
        ArrayList<String> columns = new ArrayList<>();
        while (resultSet.next()) {
            //название столбцов находится по 4-му индексу
            columns.add(resultSet.getString(4));
        }
        return columns;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
