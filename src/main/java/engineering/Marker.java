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
            ResultSet data = postgresqlAgent.select("texts", new JSONObject());
            int count = 0;
            FeatureExtractor datesExtractor = new DatesExtractor();
            FeatureExtractor textExtractor = new TextExtractor();
            JSONObject temp;
            while(data.next() && count < 2000){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", new String(data.getBytes("text"), "cp1251"));
                jsonObject.put("currentDate", new String(data.getBytes("date"), "cp1251"));
                temp = datesExtractor.extract(jsonObject);
                if(temp.has("dates")){
                    jsonObject.put("dates", temp.get("dates"));
                }
                System.out.println(jsonObject);
                temp = textExtractor.extract(jsonObject);
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
