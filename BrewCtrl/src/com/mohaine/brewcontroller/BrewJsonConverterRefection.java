package com.mohaine.brewcontroller;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.bean.ConfigurationStep;
import com.mohaine.brewcontroller.client.bean.ConfigurationStepControlPoint;
import com.mohaine.brewcontroller.client.bean.ConfigurationStepList;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.SensorConfiguration;
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

	private JsonObjectConverter jc;

	public BrewJsonConverterRefection() throws Exception {

	}

	public JsonObjectConverter getJsonConverter() throws Exception {

		if (jc == null) {
			synchronized (this) {
				if (jc == null) {
					JsonObjectConverter jc = new JsonObjectConverter(false);

					List<Class<?>> classes = getClassesToSupport();
					for (Class<?> jsonClass : classes) {
						jc.addHandler(ReflectionJsonHandler.build(jsonClass));
					}

					this.jc = jc;
				}
			}
		}
		return jc;
	}

	public List<Class<?>> getClassesToSupport() throws Exception {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(Configuration.class);
		classes.add(SensorConfiguration.class);
		classes.add(ConfigurationStepList.class);
		classes.add(ConfigurationStep.class);
		classes.add(ConfigurationStepControlPoint.class);
		classes.add(BreweryLayout.class);
		classes.add(Tank.class);
		classes.add(Sensor.class);
		classes.add(HeatElement.class);
		classes.add(Pump.class);
		classes.add(VersionBean.class);
		classes.add(ControllerStatus.class);
		classes.add(ControlStep.class);
		classes.add(TempSensor.class);
		classes.add(ControlPoint.class);
		return classes;
	}

}
