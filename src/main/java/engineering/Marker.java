package engineering;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.util.Properties;

public class Marker {
    private final Properties properties = new Properties();
    private final String DBUrl = "jdbc:postgresql://localhost:5432/fishtexts";

    public Marker() {
        properties.setProperty("user","postgres");
        properties.setProperty("password","081099");
        properties.setProperty("useUnicode","true");
        properties.setProperty("encoding", "WIN1251");

    }

    public void start(){
        try(PostgresAgent postgresqlAgent = new PostgresAgent(DBUrl, properties)) {
            System.out.println(postgresqlAgent.columns("texts"));
            ResultSet data = postgresqlAgent.select("texts", new JSONObject());
            int count = 0;
            FeatureExtractor datesExtractor = new DatesExtractor();
            FeatureExtractor textExtractor = new TextExtractor();
            while(data.next() && count < 1000){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", new String(data.getBytes("text"), "cp1251"));
                jsonObject.put("currentDate", new String(data.getBytes("date"), "cp1251"));
                jsonObject.put("dates", datesExtractor.extract(jsonObject).get("dates"));
                System.out.println(jsonObject);
                JSONObject temp = textExtractor.extract(jsonObject);
                System.out.println(temp);
                count ++;
            }
        } catch (Exception sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        Marker marker = new Marker();
        marker.start();
    }
}
