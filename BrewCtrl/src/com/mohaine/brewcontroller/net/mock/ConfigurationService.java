package com.mohaine.brewcontroller.net.mock;

import com.mohaine.brewcontroller.BrewJsonConverterRefection;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.net.mock.MockHardwareServer.HtmlService;
import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;
import com.mohaine.brewcontroller.shared.util.StringUtils;

public class ConfigurationService implements HtmlService {

	public MockHardware mock;

	public ConfigurationService(MockHardware mock) {
		super();
		this.mock = mock;
	}

	@Override
	public String getPath() {
		return "/cmd/configuration";
	}

	@Override
	public void process(HTTPRequest request, HTTPResponse response) throws Exception {
		JsonObjectConverter converter = new BrewJsonConverterRefection().getJsonConverter();
		response.setContentType("text/json");

		String configParam = request.getParameter("configuration");

		if (StringUtils.hasLength(configParam)) {
			Configuration configuration = converter.decode(configParam, Configuration.class);
			if (configuration != null) {
				mock.setConfiguration(configuration);
			} else {
				response.setStatusCode(HttpCodes.BAD_REQUEST);
				return;
			}
		}

		String encode = converter.encode(mock.getConfiguration());
		System.out.println("encode: " + encode);
		byte[] bytes = encode.getBytes();
		response.sendContent(bytes);
	}
}
