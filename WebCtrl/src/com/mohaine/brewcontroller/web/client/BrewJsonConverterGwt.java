package com.mohaine.brewcontroller.web.client;

import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;

public class BrewJsonConverterGwt implements BrewJsonConverter {

	@Override
	public JsonObjectConverter getJsonConverter() throws Exception {
		//TODO
		return new JsonObjectConverter();
	}

}
