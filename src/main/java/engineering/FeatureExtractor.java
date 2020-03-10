package engineering;

import org.json.JSONObject;

public interface FeatureExtractor {
    JSONObject extract(JSONObject data);
}
