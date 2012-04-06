package uco.sdd.utility;

import org.json.JSONArray;

public interface GetJSONListener {
	public void onRemoteCallComplete(JSONArray jsonFromNet);
}
