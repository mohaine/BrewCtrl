package com.mohaine.brewcontroller.net.mock;

import java.util.List;

import com.mohaine.brewcontroller.BrewJsonConverter;
import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.json.JsonPrettyPrint;
import com.mohaine.brewcontroller.net.mock.MockHardwareServer.HtmlService;
import com.mohaine.brewcontroller.util.StringUtils;

public class StatusService implements HtmlService {

	public MockHardware mock;

	public StatusService(MockHardware mock) {
		super();
		this.mock = mock;
	}

	@Override
	public String getPath() {
		return "/cmd/status";
	}

	@Override
	public void process(HTTPRequest request, HTTPResponse response) throws Exception {
		JsonObjectConverter converter = BrewJsonConverter.getJsonConverter();
		response.setContentType("text/json");

		{
			String modeParam = request.getParameter("mode");
			if (StringUtils.hasLength(modeParam)) {
				mock.setMode(modeParam);
			}
		}
		{
			String stepsParam = request.getParameter("steps");
			if (StringUtils.hasLength(stepsParam)) {
				List<HeaterStep> steps = converter.decodeList(stepsParam, HeaterStep.class);
				if (steps != null) {
					mock.setSteps(steps);
				} else {
					response.setStatusCode(HttpCodes.BAD_REQUEST);
					return;
				}
			}
		}
		{
			String modifyStepsParam = request.getParameter("modifySteps");
			if (StringUtils.hasLength(modifyStepsParam)) {

				List<HeaterStep> steps = converter.decodeList(modifyStepsParam, HeaterStep.class);
				if (steps != null) {
					for (HeaterStep heaterStep : steps) {
						mock.updateStep(heaterStep);
					}
				} else {
					response.setStatusCode(HttpCodes.BAD_REQUEST);
					return;
				}
			}
		}
		ControllerStatus status = mock.getStatus();

		String statusJson = converter.encode(status);
		System.out.println(new JsonPrettyPrint().prettyPrint(statusJson));

		byte[] bytes = statusJson.getBytes();
		response.sendContent(bytes);
	}

}
