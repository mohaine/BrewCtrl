package com.mohaine.brewcontroller;

import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.client.bean.VersionBean;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;
import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;
import com.mohaine.brewcontroller.shared.json.ReflectionJsonHandler;

public class BrewJsonConverterRefection implements BrewJsonConverter {
	public JsonObjectConverter getJsonConverter() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		jc.addHandler(ReflectionJsonHandler.build(Configuration.class));
		jc.addHandler(ReflectionJsonHandler.build(SensorConfiguration.class));
		jc.addHandler(ReflectionJsonHandler.build(ConfigurationStepList.class));
		jc.addHandler(ReflectionJsonHandler.build(ConfigurationHeaterStep.class));
		jc.addHandler(ReflectionJsonHandler.build(ConfigurationHeaterStepControlPoint.class));
		jc.addHandler(ReflectionJsonHandler.build(BreweryLayout.class));
		jc.addHandler(ReflectionJsonHandler.build(Tank.class));
		jc.addHandler(ReflectionJsonHandler.build(Sensor.class));
		jc.addHandler(ReflectionJsonHandler.build(HeatElement.class));
		jc.addHandler(ReflectionJsonHandler.build(Pump.class));

		jc.addHandler(ReflectionJsonHandler.build(VersionBean.class));

		jc.addHandler(ReflectionJsonHandler.build(ControllerStatus.class));
		jc.addHandler(ReflectionJsonHandler.build(ControlStep.class));
		jc.addHandler(ReflectionJsonHandler.build(TempSensor.class));
		jc.addHandler(ReflectionJsonHandler.build(ControlPoint.class));

		return jc;
	}
}
