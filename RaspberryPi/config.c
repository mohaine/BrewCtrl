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

BreweryLayout * parseBrewLayout(json_object *layout) {
	boolean valid = false;
	BreweryLayout * bl = malloc(sizeof(BreweryLayout));

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
		free(bl);
		bl = NULL;
	}

	return bl;
}

Configuration * parseJsonConfiguration(byte *data) {
	boolean valid = false;
	Configuration * cfg = malloc(sizeof(Configuration));

	json_object *config = json_tokener_parse(data);
	if (config != NULL) {

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

		}

		json_object_put(config);
	}

	if (!valid) {
		free(cfg);
		cfg = NULL;
	}
	return cfg;

}

bool initConfiguration() {
	bool valid = false;

	struct stat st;
	if (stat(CONFIG_FILE, &st) >= 0) {

		char* tmp = malloc(BUFFER_SIZE);
		if (tmp == NULL) {
			exit(-1);
		}
		ssize_t s = st.st_size;
		if (s > BUFFER_SIZE) {
			s = BUFFER_SIZE;
		}
		FILE* f = fopen(CONFIG_FILE, "rb");
		if (f) {
			int readSize = fread(tmp, 1, s, f);
			if (readSize == s) {
				//TODO PARSE

				printf("Read in file\n");

				Configuration * newCfg = parseJsonConfiguration(tmp);

				if (newCfg != NULL) {
					config = newCfg;
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
	config = newConfig;
}
Configuration * getConfiguration() {
	return config;
}

