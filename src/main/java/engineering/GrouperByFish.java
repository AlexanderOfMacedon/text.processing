package engineering;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class GrouperByFish {
    private final Properties properties = new Properties();
    private final String DBUrl = "jdbc:postgresql://localhost:5432/fishtexts";

    public GrouperByFish() {
        properties.setProperty("user", "postgres");
        properties.setProperty("password", "081099");
        properties.setProperty("useUnicode", "true");
        properties.setProperty("encoding", "WIN1251");

    }

    public static void main(String[] args) throws Exception {
        GrouperByFish grouper = new GrouperByFish();
        grouper.start();
    }

    public void start() {
        try (PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            ResultSet data = postgresqlAgent.select("parsed_texts", new JSONObject());
            FeatureExtractor fishExtractor = new FishExtractor();
            int count = 0;
            while (data.next() && count < 100) {
                fishExtractor.extract(getRow(data));
                count++;
                if(count % 10000 == 0){
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
        row.put("date", new String(data.getBytes("date"), "cp1251"));
        row.put("main_place", new String(data.getBytes("main_place"), "cp1251"));
        row.put("mini_place", new String(data.getBytes("mini_place"), "cp1251"));
        row.put("is_dialog", data.getBoolean("is_dialog"));
        row.put("marked", new String(data.getBytes("marked"), "cp1251"));
        row.put("iscomment", new String(data.getBytes("iscomment"), "cp1251"));
        return row;
    }
}
