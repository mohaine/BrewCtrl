package com.mohaine.brewcontroller;

import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.bean.VersionBean;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.json.ReflectionJsonHandler;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;

public class BrewJsonConverter {
	public static JsonObjectConverter getJsonConverter() throws Exception {
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
		jc.addHandler(ReflectionJsonHandler.build(HeaterStep.class));
		jc.addHandler(ReflectionJsonHandler.build(HardwareSensor.class));
		jc.addHandler(ReflectionJsonHandler.build(ControlPoint.class));

		return jc;
	}
}
