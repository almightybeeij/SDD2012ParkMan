package uco.sdd.utility;

import org.json.JSONArray;
import org.json.JSONObject;

public interface GetJSONListener {
	
	public void onRemoteCallComplete(JSONArray jsonFromNet);
	
	public void onRemoteCallComplete(JSONObject jsonFromNet);
}
