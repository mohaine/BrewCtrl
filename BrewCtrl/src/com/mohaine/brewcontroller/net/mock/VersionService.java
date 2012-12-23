package com.mohaine.brewcontroller.net.mock;

import com.mohaine.brewcontroller.bean.VersionBean;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.json.ReflectionJsonHandler;
import com.mohaine.brewcontroller.net.mock.MockHardwareServer.HtmlService;

public class VersionService implements HtmlService {
	@Override
	public String getPath() {
		return "/cmd/version";
	}

	@Override
	public void process(HTTPRequest request, HTTPResponse response) throws Exception {
		JsonObjectConverter converter = getConverter();
		VersionBean versionBean = new VersionBean();
		versionBean.setVersion("1.0");
		response.setContentType("text/json");
		byte[] bytes = converter.encode(versionBean).getBytes();
		response.sendContent(bytes);
	}

	private JsonObjectConverter getConverter() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		jc.addHandler(ReflectionJsonHandler.build(VersionBean.class));
		return jc;
	}
}