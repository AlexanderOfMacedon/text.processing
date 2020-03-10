package engineering;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBUtils {
    public static ArrayList<JSONObject> resultSet2Json(ResultSet resultSet, ArrayList<String> columns)
            throws SQLException, UnsupportedEncodingException {
        JSONObject jsonObject;
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
        while (resultSet.next()) {
            jsonObject = new JSONObject();
            for (String column : columns) {
                jsonObject.put(column, new String(resultSet.getBytes(column), "UTF-8"));
            }
            jsonObjectArrayList.add(jsonObject);
        }
        return jsonObjectArrayList;
    }
}
