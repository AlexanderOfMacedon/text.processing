package engineering;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

public class GrouperByFish {
    private final Properties properties = new Properties();
    private final String DBUrl;
    private final String sourceTableName;

    public GrouperByFish(String databaseName, String sourceTableName) {
        DBUrl = "jdbc:postgresql://localhost:5432/" + databaseName;
        properties.setProperty("user", "postgres");
        properties.setProperty("password", "081099");
        properties.setProperty("useUnicode", "true");
        properties.setProperty("encoding", "WIN1251");
        this.sourceTableName = sourceTableName;
    }

    public static void main(String[] args) throws Exception {
        GrouperByFish grouper = new GrouperByFish("fishtexts", "comments");
        grouper.start();
    }

    public void start() {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            ResultSet data = postgresqlAgent.select(sourceTableName, new JSONObject());
            FeatureExtractor fishExtractor = new FishExtractor();
            Multimap<String, JSONObject> rows = collectRows(data, fishExtractor);
            insertRows(postgresqlAgent, rows);
        } catch (Exception sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    private JSONObject getRow(ResultSet data) throws SQLException, UnsupportedEncodingException {
        JSONObject row = new JSONObject();
        row.put("text", new String(data.getBytes("text"), "utf-8"));
        row.put("date", new String(data.getBytes("date"), "utf-8"));
        row.put("main_place", new String(data.getBytes("main_place"), "utf-8"));
        row.put("mini_place", new String(data.getBytes("mini_place"), "utf-8"));
        row.put("marked", new String(data.getBytes("marked"), "utf-8"));
        return row;
    }

    private JSONObject cleanRow(JSONObject row, Set<String> columns) throws UnsupportedEncodingException {
        JSONObject cleanRow = new JSONObject();
        for(String column: columns){
            if(row.has(column)){
                cleanRow.put(column, new String(row.get(column).toString().getBytes(), "UTF-8"));
            }
        }
        return cleanRow;
    }

    private void insertRows(PostgresAgent postgresAgent, Multimap<String, JSONObject> rows)
            throws UnsupportedEncodingException, SQLException {
        JSONObject columns = new JSONObject() {{
            put("main_place", "TEXT");
            put("mini_place", "TEXT");
            put("text", "TEXT");
            put("date", "varchar(16)");
            put("marked", "varchar(3)");
        }};
        for(String fish: rows.keySet()){
            String tableName = new String(fish.toString().getBytes(), "UTF-8");
            try {
                postgresAgent.createTable(tableName, columns);
            } catch (Exception ignore){}
            for(JSONObject row: rows.get(fish)){
                postgresAgent.insert(tableName, cleanRow(row, columns.keySet()));
            }
        }
    }

    private Multimap<String, JSONObject> collectRows(ResultSet data, FeatureExtractor fishExtractor)
            throws SQLException, UnsupportedEncodingException {
        Multimap<String, JSONObject> fishCollects = ArrayListMultimap.create();
        JSONObject row, features;
        while(data.next()){
            row = getRow(data);
            features = fishExtractor.extract(row);
            if(features.length() > 0){
                for(String fish : features.keySet()){
                    fishCollects.put(fish, collectFishRow(row, features.getString(fish)));
                }
            }
        }
        return fishCollects;
    }

    private JSONObject collectFishRow(JSONObject mainRow, String text){
        JSONObject row = new JSONObject(mainRow, JSONObject.getNames(mainRow));
        row.put("text", text);
        return row;
    }
}
