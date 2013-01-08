/*
 Copyright 2009-2011 Michael Graessle
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

#include "sensor.h"
#include "config.h"
#include "logger.h"

#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>
#include <json.h>
#define BUFFER_SIZE 1024*200

#define CONFIG_FILE "BrewControllerConfig.json"

Configuration * config = NULL;

void appendBrewLayout(json_object *config, BreweryLayout * bl) {
	json_object *layout = json_object_new_object();
	json_object_object_add(config, "brewLayout", layout);

	json_object_object_add(layout, "maxAmps", json_object_new_int(bl->maxAmps));

	json_object *tanks = json_object_new_array();

	json_object_object_add(layout, "tanks", tanks);

	Tank * tA = (Tank *) bl->tanks.data;
	for (int i = 0; i < bl->tanks.count; i++) {
		json_object *tank = json_object_new_object();
		json_object_array_add(tanks, tank);
		Tank * t = &tA[i];
		json_object_object_add(tank, "name", json_object_new_string(t->name));

		if (t->sensor != NULL) {
			json_object *sensor = json_object_new_object();
			json_object_object_add(tank, "sensor", sensor);

			json_object_object_add(sensor, "address", json_object_new_string(t->sensor->address));
		}
		if (t->heater != NULL) {
			json_object *heater = json_object_new_object();
			json_object_object_add(tank, "heater", heater);

			json_object_object_add(heater, "name", json_object_new_string(t->heater->name));
			json_object_object_add(heater, "pin", json_object_new_int(t->heater->pin));
			json_object_object_add(heater, "hasDuty", json_object_new_boolean(t->heater->hasDuty));

		}

	}

}

char* formatJsonConfiguration(Configuration * cfg) {
	json_object *config = json_object_new_object();

	json_object_object_add(config, "version", json_object_new_string(cfg->version));
	json_object_object_add(config, "logMessages", json_object_new_boolean(cfg->logMessages));
	appendBrewLayout(config, cfg->brewLayout);

	json_object *sensors = json_object_new_array();
	json_object_object_add(config, "sensors", sensors);

	SensorConfig * tA = (SensorConfig *) cfg->sensors.data;
	for (int i = 0; i < cfg->sensors.count; i++) {
		json_object *sensor = json_object_new_object();
		json_object_array_add(sensors, sensor);
		SensorConfig * t = &tA[i];
		json_object_object_add(sensor, "name", json_object_new_string(t->name));
		json_object_object_add(sensor, "location", json_object_new_string(t->location));
		json_object_object_add(sensor, "address", json_object_new_string(t->address));
	}

	json_object *stepLists = json_object_new_array();
	json_object_object_add(config, "stepLists", stepLists);

	StepList * stA = (StepList *) cfg->stepLists.data;
	for (int slI = 0; slI < cfg->stepLists.count; slI++) {
		json_object *stepList = json_object_new_object();
		json_object_array_add(stepLists, stepList);
		StepList * t = &stA[slI];
		json_object_object_add(stepList, "name", json_object_new_string(t->name));

		json_object *steps = json_object_new_array();
		json_object_object_add(stepList, "steps", steps);

		Step * slsA = (Step *) t->steps.data;
		for (int slsI = 0; slsI < t->steps.count; slsI++) {
			json_object *step = json_object_new_object();
			json_object_array_add(steps, step);
			Step * s = &slsA[slsI];

			json_object_object_add(step, "name", json_object_new_string(s->name));
			json_object_object_add(step, "time", json_object_new_string(s->time));

			json_object *controlPoints = json_object_new_array();
			json_object_object_add(step, "controlPoints", controlPoints);

			StepControlPoint * stCpA = (StepControlPoint *) s->controlPoints.data;
			for (int sCpI = 0; sCpI < s->controlPoints.count; sCpI++) {
				json_object *controlPoint = json_object_new_object();
				json_object_array_add(controlPoints, controlPoint);
				StepControlPoint * cp = &stCpA[sCpI];
				json_object_object_add(controlPoint, "controlName", json_object_new_string(cp->controlName));
				json_object_object_add(controlPoint, "targetName", json_object_new_string(cp->targetName));
				json_object_object_add(controlPoint, "targetTemp", json_object_new_double(cp->targetTemp));

			}

		}

	}

	const char * tmp = json_object_get_string(config);
	int length = strlen(tmp);

	char * buffer = malloc(length + 1);

	if (buffer == NULL) {
		exit(-1);
	}
	memcpy(buffer, tmp, length + 1);
	json_object_put(config);
	return buffer;
}

char* mallocString(json_object *obj) {
	const char * tmp;
	tmp = json_object_get_string(obj);
	int length = strlen(tmp);
	char * returnValue = malloc(length + 1);
	if (returnValue == NULL) {
		exit(-1);
	}
	memcpy(returnValue, tmp, length + 1);
	return returnValue;
}

HeatElement * parseHeatElement(json_object *layout) {
	boolean valid = false;
	HeatElement * s = malloc(sizeof(HeatElement));

	if (json_object_get_type(layout) == json_type_object) {
		valid = true;
		json_object * value;
		value = json_object_object_get(layout, "name");
		if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
			s->name = mallocString(value);
		} else {
			valid = false;
		}
		value = json_object_object_get(layout, "pin");
		if (valid && value != NULL && json_object_get_type(value) == json_type_int) {
			s->pin = json_object_get_int(value);
		} else {
			valid = false;
		}
		value = json_object_object_get(layout, "hasDuty");
		if (valid && value != NULL && json_object_get_type(value) == json_type_boolean) {
			s->hasDuty = json_object_get_boolean(value);
		} else {
			valid = false;
		}

	}
	if (!valid) {
		free(s);
		s = NULL;
	}
	return s;
}

Sensor * parseSensor(json_object *layout) {
	boolean valid = false;
	Sensor * s = malloc(sizeof(Sensor));

	if (json_object_get_type(layout) == json_type_object) {
		valid = true;
		json_object * value;
		value = json_object_object_get(layout, "address");
		if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
			s->address = mallocString(value);
		} else {
			valid = false;
		}

	}
	if (!valid) {
		free(s);
		s = NULL;
	}
	return s;
}

void freeIfNotNull(void* p) {
	if (p != NULL) {
		free(p);
	}

}
void freeSensor(Sensor * s) {
	if (s != NULL) {
		freeIfNotNull(s->address);
	}
	freeIfNotNull(s);
}
void freeHeater(HeatElement * h) {
	if (h != NULL) {
		freeIfNotNull(h->name);
	}
	freeIfNotNull(h);
}

void freeTankChildren(Tank * t) {
	freeHeater(t->heater);
	freeSensor(t->sensor);
	freeIfNotNull(t->name);
}
void freeStepControlPointChildren(StepControlPoint * t) {
	freeIfNotNull(t->controlName);
	freeIfNotNull(t->targetName);
}

void freeBrewLayout(BreweryLayout * bl) {
	if (bl->tanks.data != NULL) {

		Tank * tA = (Tank *) bl->tanks.data;
		for (int i = 0; i < bl->tanks.count; i++) {
			Tank * t = &tA[i];

			freeTankChildren(t);
		}

		free(bl->tanks.data);
		bl->tanks.data = NULL;
	}
	free(bl);
}

void freeStepChildren(Step * s) {
	freeIfNotNull(s->name);
	freeIfNotNull(s->time);
	if (s->controlPoints.data != NULL) {
		StepControlPoint * tA = (StepControlPoint*) s->controlPoints.data;
		for (int i = 0; i < s->controlPoints.count; i++) {
			StepControlPoint * t = &tA[i];

			freeStepControlPointChildren(t);
		}
		free(s->controlPoints.data);
	}

}
void freeStepListChildren(StepList * s) {
	freeIfNotNull(s->name);
	if (s->steps.data != NULL) {
		Step * tA = (Step*) s->steps.data;
		for (int i = 0; i < s->steps.count; i++) {
			Step * t = &tA[i];
			freeStepChildren(t);
		}
		free(s->steps.data);
	}

}

void freeConfiguration(Configuration * c) {
	if (c->brewLayout != NULL) {
		freeBrewLayout(c->brewLayout);
	}
	if (c->sensors.data != NULL) {
		SensorConfig * tA = (SensorConfig *) c->sensors.data;
		for (int i = 0; i < c->sensors.count; i++) {
			SensorConfig * t = &tA[i];

			freeIfNotNull(t->address);
			freeIfNotNull(t->name);
			freeIfNotNull(t->location);
		}

		free(c->sensors.data);
	}

	//TODO
	if (c->stepLists.data != NULL) {
		StepList * tA = (StepList *) c->stepLists.data;
		for (int i = 0; i < c->stepLists.count; i++) {
			StepList * t = &tA[i];
			freeStepListChildren(t);
		}

		free(c->stepLists.data);
	}
	freeIfNotNull(c->version);

	free(c);

}
bool parseStepControlPoint(StepControlPoint * cp, json_object *controlPoint) {
	bool valid = true;
	json_object * value;

	cp->targetName = NULL;
	cp->controlName = NULL;

	value = json_object_object_get(controlPoint, "targetName");
	if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
		cp->targetName = mallocString(value);
	} else {
		DBG("parseJsonConfiguration ControlPoint Missing targetName\n");
		valid = false;
	}
	value = json_object_object_get(controlPoint, "controlName");
	if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
		cp->controlName = mallocString(value);
	} else {
		DBG("parseJsonConfiguration ControlPoint Missing controlName\n");
		valid = false;
	}
	value = json_object_object_get(controlPoint, "targetTemp");
	if (valid && value != NULL && json_object_get_type(value) == json_type_double) {
		cp->targetTemp = json_object_get_double(value);
	} else {
		DBG("parseJsonConfiguration ControlPoint Missing targetTemp\n");
		valid = false;
	}
	return valid;
}
BreweryLayout * parseBrewLayout(json_object *layout) {

	DBG("parseBrewLayout\n");

	boolean valid = false;
	BreweryLayout * bl = malloc(sizeof(BreweryLayout));
	bl->tanks.data = NULL;
	if (json_object_get_type(layout) == json_type_object) {
		valid = true;
		json_object * value;

		value = json_object_object_get(layout, "maxAmps");
		if (valid && value != NULL && json_object_get_type(value) == json_type_int) {
			bl->maxAmps = json_object_get_int(value);
		} else {
			valid = false;
		}

		value = json_object_object_get(layout, "tanks");
		if (valid && value != NULL && json_object_get_type(value) == json_type_array) {
			json_object * tanks = value;

			bl->tanks.count = json_object_array_length(tanks);
			bl->tanks.data = malloc(sizeof(Tank) * bl->tanks.count);
			Tank * tA = (Tank *) bl->tanks.data;

			for (int i = 0; i < bl->tanks.count; i++) {

				Tank * t = &tA[i];

				t->name = NULL;
				t->sensor = NULL;
				t->heater = NULL;

				json_object *tank = json_object_array_get_idx(tanks, i);
				value = json_object_object_get(tank, "name");
				if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
					t->name = mallocString(value);
				} else {
					valid = false;
					break;
				}

				value = json_object_object_get(tank, "sensor");
				if (valid && value != NULL && json_object_get_type(value) == json_type_object) {
					t->sensor = parseSensor(value);
				} else {
					t->sensor = NULL;
				}

				value = json_object_object_get(tank, "heater");
				if (valid && value != NULL && json_object_get_type(value) == json_type_object) {
					t->heater = parseHeatElement(value);
				} else {
					t->heater = NULL;
				}

			}

		} else {
			valid = false;
		}

	}

//	printf("11l->tanks.data: %d,%d\n", bl->tanks.count, bl->tanks.data);
//	Tank * data = (Tank *) bl->tanks.data;
//	for (int i = 0; i < bl->tanks.count; i++) {
//		Tank t = data[i];
//		printf("%d %s\n", &t, t.name);
//	}

	if (!valid) {

		DBG("Invalid BrewLayout\n");

		freeBrewLayout(bl);
		bl = NULL;
	}

	return bl;
}

Configuration * parseJsonConfiguration(byte *data) {

	DBG("parseJsonConfiguration Entry\n");

	boolean valid = false;
	Configuration * cfg = malloc(sizeof(Configuration));

	cfg->version = NULL;
	cfg->sensors.count = 0;
	cfg->sensors.data = NULL;
	cfg->stepLists.count = 0;
	cfg->stepLists.data = NULL;
	cfg->brewLayout = NULL;

	json_object *config = json_tokener_parse(data);
	if (config != NULL) {

		DBG("parseJsonConfiguration JSON Valid\n");

		if (json_object_get_type(config) == json_type_object) {
			valid = true;
			json_object * value = json_object_object_get(config, "version");
			if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
				cfg->version = mallocString(value);
			} else {
				valid = false;
			}

			value = json_object_object_get(config, "logMessages");
			if (valid && value != NULL && json_object_get_type(value) == json_type_boolean) {
				cfg->logMessages = json_object_get_boolean(value);
			} else {
				cfg->logMessages = false;
			}
			value = json_object_object_get(config, "brewLayout");
			if (valid && value != NULL && json_object_get_type(value) == json_type_object) {
				cfg->brewLayout = parseBrewLayout(value);
				valid = cfg->brewLayout != NULL;
			} else {
				valid = false;
			}
			value = json_object_object_get(config, "sensors");
			if (valid && value != NULL && json_object_get_type(value) == json_type_array) {
				json_object * tanks = value;

				cfg->sensors.count = json_object_array_length(tanks);
				cfg->sensors.data = malloc(sizeof(SensorConfig) * cfg->sensors.count);
				SensorConfig * tA = (SensorConfig *) cfg->sensors.data;

				for (int i = 0; i < cfg->sensors.count; i++) {

					SensorConfig * sc = &tA[i];

					sc->name = NULL;
					sc->location = NULL;
					sc->address = NULL;

					json_object *sensor = json_object_array_get_idx(tanks, i);
					value = json_object_object_get(sensor, "name");
					if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
						sc->name = mallocString(value);
					} else {
						DBG("parseJsonConfiguration SensorConfig Missing Name\n");

						valid = false;
						break;
					}

					value = json_object_object_get(sensor, "location");
					if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
						sc->location = mallocString(value);
					} else {
						DBG("parseJsonConfiguration SensorConfig Missing Location\n");
						valid = false;
						break;
					}

					value = json_object_object_get(sensor, "address");
					if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
						sc->address = mallocString(value);
					} else {
						DBG("parseJsonConfiguration SensorConfig Missing Address\n");
						valid = false;
						break;
					}

				}

			} else {
				DBG("parseJsonConfiguration Missing Sensors\n");
				valid = false;
			}
			value = json_object_object_get(config, "stepLists");
			if (valid && value != NULL && json_object_get_type(value) == json_type_array) {
				json_object * stepLists = value;

				cfg->stepLists.count = json_object_array_length(stepLists);
				cfg->stepLists.data = malloc(sizeof(StepList) * cfg->stepLists.count);
				StepList * slA = (StepList *) cfg->stepLists.data;

				for (int slI = 0; slI < cfg->stepLists.count; slI++) {

					StepList * sl = &slA[slI];

					sl->name = NULL;
					sl->steps.count = 0;
					sl->steps.data = NULL;

					json_object *stepList = json_object_array_get_idx(stepLists, slI);
					value = json_object_object_get(stepList, "name");
					if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
						sl->name = mallocString(value);
					} else {
						valid = false;
						break;
					}
					value = json_object_object_get(stepList, "steps");
					if (valid && value != NULL && json_object_get_type(value) == json_type_array) {
						json_object * steps = value;

						sl->steps.count = json_object_array_length(steps);
						sl->steps.data = malloc(sizeof(Step) * sl->steps.count);
						Step * slA = (Step *) sl->steps.data;

						for (int slsI = 0; slsI < sl->steps.count; slsI++) {

							Step * s = &slA[slsI];

							s->name = NULL;
							s->controlPoints.count = 0;
							s->controlPoints.data = NULL;
							s->time = NULL;

							json_object *step = json_object_array_get_idx(steps, slsI);
							value = json_object_object_get(step, "name");
							if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
								s->name = mallocString(value);
							} else {
								valid = false;
								break;
							}
							value = json_object_object_get(step, "time");
							if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
								s->time = mallocString(value);
							} else {
								valid = false;
								break;
							}

							value = json_object_object_get(step, "controlPoints");
							if (valid && value != NULL && json_object_get_type(value) == json_type_array) {
								json_object * controlPoints = value;

								s->controlPoints.count = json_object_array_length(controlPoints);
								s->controlPoints.data = malloc(sizeof(StepControlPoint) * s->controlPoints.count);
								StepControlPoint * slCpA = (StepControlPoint *) s->controlPoints.data;

								for (int slcpI = 0; slcpI < s->controlPoints.count; slcpI++) {

									StepControlPoint * cp = &slCpA[slcpI];
									json_object *controlPoint = json_object_array_get_idx(controlPoints, slcpI);

									valid = parseStepControlPoint(cp, controlPoint);
									if (!valid) {
										break;
									}

								}
							} else {
								DBG("parseJsonConfiguration Step Missing controlPoints\n");
								valid = false;
							}
						}
					} else {
						DBG("parseJsonConfiguration StepLists Missing Steps\n");

						valid = false;
					}
				}

			} else {
				DBG("parseJsonConfiguration Config Missing StepLists\n");
				valid = false;
			}

		}

		json_object_put(config);
	}

	if (!valid) {
		freeConfiguration(cfg);
		cfg = NULL;
	}
	return cfg;

}

bool initConfiguration() {
	bool valid = false;

	struct stat st;
	if (stat(CONFIG_FILE, &st) >= 0) {

		ssize_t s = st.st_size;
		if (s > BUFFER_SIZE - 1) {
			s = BUFFER_SIZE - 1;
		}
		char* tmp = malloc(s + 12);
		if (tmp == NULL) {
			exit(-1);
		}

		FILE* f = fopen(CONFIG_FILE, "rb");
		if (f) {
			int readSize = fread(tmp, 1, s, f);
			if (readSize == s) {
				tmp[s] = '\0';
				DBG("Parse file %s size: %d\n", CONFIG_FILE,s);

				Configuration * newCfg = parseJsonConfiguration(tmp);

				if (newCfg != NULL) {
					setConfiguration(newCfg);
					valid = true;
				}
			}
			fclose(f);
		}
		free(tmp);
	}
	return valid;
}
void setConfiguration(Configuration * newConfig) {

	if (config != NULL) {
		freeConfiguration(config);
		config = NULL;
	}

	config = newConfig;
}
Configuration * getConfiguration() {
	return config;
}

