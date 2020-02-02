package engineering;

import org.json.JSONObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

public class PostgresAgentTest {
    private final Properties properties = new Properties();
    private final String DBUrl = "jdbc:postgresql://localhost:5432/test_java";

    public PostgresAgentTest() {
        properties.setProperty("user","postgres");
        properties.setProperty("password","081099");
        properties.setProperty("useUnicode","true");
        properties.setProperty("encoding", "WIN1251");

    }

    @Test
    public void select() throws Exception {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            ResultSet data = postgresqlAgent.select("table1", new JSONObject());
            ArrayList<JSONObject> jsonData = DBUtils.resultSet2Json(data, postgresqlAgent.columns("table1"));
            System.out.println(jsonData);
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    @Test
    public void createTable() {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("column1", "varchar(1000)");
            jsonObject.accumulate("column2", "TEXT");
            postgresqlAgent.createTable("table1", jsonObject);

        } catch (Exception sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    @Test
    public void insert() {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("column1", new String("ываыв".getBytes(), "UTF-8"));
            jsonObject.accumulate("column2", new String("ывафыва".getBytes(), "UTF-8"));
            postgresqlAgent.insert("table1", jsonObject);

        } catch (Exception sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    @Test
    public void columns() {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            ArrayList<String> columns = postgresqlAgent.columns("table1");
            System.out.println(columns.toString());

        } catch (Exception sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    @Test
    public void deleteTable() {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            postgresqlAgent.deleteTable("table1");

        } catch (Exception sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

}