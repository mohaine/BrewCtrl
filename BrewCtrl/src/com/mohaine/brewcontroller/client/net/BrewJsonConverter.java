package com.mohaine.brewcontroller.client.net;

import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;

public interface BrewJsonConverter {
	public JsonObjectConverter getJsonConverter() throws Exception;
}
