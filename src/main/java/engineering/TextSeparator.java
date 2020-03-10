package engineering;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class TextSeparator {
    private final Properties properties = new Properties();
    private final String DBUrl;
    private final String sourceTableName;
    private final String distTableName;

    public TextSeparator(String databaseName, String sourceTableName, String distTableName) {
        DBUrl = "jdbc:postgresql://localhost:5432/" + databaseName;
        properties.setProperty("user", "postgres");
        properties.setProperty("password", "081099");
        properties.setProperty("useUnicode", "true");
        properties.setProperty("encoding", "WIN1251");
        this.sourceTableName = sourceTableName;
        this.distTableName = distTableName;
    }


    public static void main(String[] args) throws Exception {
        TextSeparator separator = new TextSeparator("fishtexts", "texts",
                "parsed_texts");
        separator.start();
    }

    public void start() {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            ResultSet data = postgresqlAgent.select(sourceTableName, new JSONObject());
            int count = 0;
            while (data.next()) {
//                System.out.println(getRow(data));
                JSONObject separatedRow = getSeparatedRow(getRow(data));
                if (separatedRow != null) {
                    for (JSONObject row : getHandledRows(separatedRow)) {
                        insertJson(postgresqlAgent, row);
                    }
                }
                count++;
                if (count % 10000 == 0) {
                    System.out.println("Handled: " + count);
                }
            }
        } catch (Exception sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    private JSONObject getRow(ResultSet data) throws SQLException, UnsupportedEncodingException {
        JSONObject row = new JSONObject();
        row.put("text", new String(data.getBytes("text"), "cp1251"));
        row.put("currentDate", new String(data.getBytes("date"), "cp1251"));
        row.put("main_place", new String(data.getBytes("main_place"), "cp1251"));
        row.put("mini_place", new String(data.getBytes("mini_place"), "cp1251"));
        row.put("is_dialog", data.getBoolean("is_dialog"));
        row.put("marked", new String(data.getBytes("marked"), "cp1251"));
        row.put("iscomment", new String(data.getBytes("iscomment"), "cp1251"));
        return row;
    }

    private JSONObject getSeparatedRow(JSONObject row) {
        FeatureExtractor datesExtractor = new DatesExtractor();
        FeatureExtractor textExtractor = new TextExtractor();
        JSONObject sepatatedRow = row;
        JSONObject temp = datesExtractor.extract(sepatatedRow);
        if (temp.has("dates")) {
            sepatatedRow.put("dates", temp.get("dates"));
            temp = textExtractor.extract(sepatatedRow);
            if (temp.has("texts")) {
                sepatatedRow.put("texts", temp.get("texts"));
                return sepatatedRow;
            }
        }
        return null;
    }

    private Set<JSONObject> getHandledRows(JSONObject separatedRow) throws IOException, JsonMappingException {
        Set<JSONObject> rows = new HashSet<>();
        JSONObject baseJson = new JSONObject() {{
            put("main_place", separatedRow.get("main_place"));
            put("mini_place", separatedRow.get("mini_place"));
            put("marked", separatedRow.get("marked"));
            put("is_dialog", separatedRow.get("is_dialog"));
            put("iscomment", separatedRow.get("iscomment"));
        }};
        JSONObject temp;
        ObjectMapper mapper = new ObjectMapper();
        if (separatedRow.has("texts")) {
            HashMap<String, Object> texts =
                    mapper.readValue(separatedRow.get("texts").toString(), HashMap.class);
            for (String date : texts.keySet()) {
                temp = new JSONObject(baseJson, JSONObject.getNames(baseJson));
                temp.put("date", date);
                temp.put("text", texts.get(date));
                rows.add(temp);
            }
            return rows;
        }
        return null;
    }

    private void insertJson(PostgresAgent postgresAgent, JSONObject row) throws SQLException, UnsupportedEncodingException {
        JSONObject columns = new JSONObject() {{
            put("main_place", "TEXT");
            put("mini_place", "TEXT");
            put("text", "TEXT");
            put("date", "varchar(16)");
            put("is_dialog", "TEXT");
            put("iscomment", "varchar(3)");
            put("marked", "varchar(3)");
        }};
        try {
            postgresAgent.createTable(distTableName, columns);
        } catch (Exception ignore) {
        }
        JSONObject cleanRow = new JSONObject();
        for (String key : row.keySet()) {
            cleanRow.put(key, new String(row.get(key).toString().getBytes(), "UTF-8"));
        }
        postgresAgent.insert(distTableName, cleanRow);
    }


}
