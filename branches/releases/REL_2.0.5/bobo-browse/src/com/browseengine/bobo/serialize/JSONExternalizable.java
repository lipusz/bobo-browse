package com.browseengine.bobo.serialize;

import org.json.JSONObject;

public interface JSONExternalizable extends JSONSerializable {
	JSONObject toJSON() throws JSONSerializationException;
	void fromJSON(JSONObject obj) throws JSONSerializationException;
}
