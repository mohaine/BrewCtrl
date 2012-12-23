package com.mohaine.brewcontroller.net.mock;

import com.mohaine.brewcontroller.BrewJsonConverter;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.net.mock.MockHardwareServer.HtmlService;
import com.mohaine.brewcontroller.util.StringUtils;

public class LayoutService implements HtmlService {

	public MockHardware mock;

	public LayoutService(MockHardware mock) {
		super();
		this.mock = mock;
	}

	@Override
	public String getPath() {
		return "/cmd/layout";
	}

	@Override
	public void process(HTTPRequest request, HTTPResponse response) throws Exception {
		JsonObjectConverter converter = BrewJsonConverter.getJsonConverter();
		response.setContentType("text/json");

		String layoutParam = request.getParameter("layout");

		if (StringUtils.hasLength(layoutParam)) {
			BreweryLayout layout = converter.decode(layoutParam, BreweryLayout.class);
			if (layout != null) {
				mock.setLayout(layout);
			} else {
				response.setStatusCode(HttpCodes.BAD_REQUEST);
				return;
			}
		}

		byte[] bytes = converter.encode(mock.getLayout()).getBytes();
		response.sendContent(bytes);
	}

}
