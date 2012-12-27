package com.mohaine.brewcontroller.net.mock;

import java.util.List;

import com.mohaine.brewcontroller.BrewJsonConverterRefection;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.net.mock.MockHardwareServer.HtmlService;
import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;
import com.mohaine.brewcontroller.shared.util.StringUtils;

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
		JsonObjectConverter converter = new BrewJsonConverterRefection().getJsonConverter();
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
				List<ControlStep> steps = converter.decodeList(stepsParam, ControlStep.class);
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

				List<ControlStep> steps = converter.decodeList(modifyStepsParam, ControlStep.class);
				if (steps != null) {
					for (ControlStep heaterStep : steps) {
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

		byte[] bytes = statusJson.getBytes();
		response.sendContent(bytes);
	}
}
