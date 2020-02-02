package engineering;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public interface DatabaseAgent extends AutoCloseable {
    /**
     * select from table of database
     * @param tableName
     * @param selectOptions is json with differently options for select query
     * @return resultSet
     */
    ResultSet select(String tableName, JSONObject selectOptions) throws SQLException;

    /**
     * insert into table of database
     * @param tableName
     * @param row
     */
    void insert(String tableName, JSONObject row) throws SQLException;

    /**
     * create table in database
     * @param tableName
     * @param columnsOptions
     */
    void createTable(String tableName, JSONObject columnsOptions) throws SQLException;

    /**
     * delete table from database
     * @param tableName
     */
    void deleteTable(String tableName) throws SQLException;

    /**
     * get set of columns name
     * @param tableName
     * @return
     */
    ArrayList<String> columns(String tableName) throws SQLException, UnsupportedEncodingException;
}
