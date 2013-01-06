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
	json_object_object_add(config, "brewLayout",layout);
	json_object_object_add(layout, "maxAmps", json_object_new_int(bl->maxAmps));


}

char* formatJsonConfiguration(Configuration * cfg) {
	json_object *config = json_object_new_object();

	json_object_object_add(config, "version", json_object_new_string(cfg->version));
	json_object_object_add(config, "logMessages", json_object_new_boolean(cfg->logMessages));

	appendBrewLayout(config, cfg->brewLayout);

	char * tmp = json_object_get_string(config);
	int length = strlen(tmp);

	char * buffer = malloc(length + 1);

	if (buffer == NULL) {
		exit(-1);
	}
	memcpy(buffer, tmp, length + 1);
	json_object_put(config);
	return buffer;
}

BreweryLayout * parseBrewLayout(json_object *layout) {
	boolean valid = false;
	BreweryLayout * br = malloc(sizeof(BreweryLayout));

	if (json_object_get_type(layout) == json_type_object) {
		valid = true;
		json_object * value;

		value = json_object_object_get(layout, "maxAmps");
		if (valid && value != NULL && json_object_get_type(value) == json_type_int) {
			br->maxAmps = json_object_get_int(value);
		} else {
			valid = false;
		}
	}
	if (!valid) {
		free(br);
		br = NULL;
	}
	return br;
}

Configuration * parseJsonConfiguration(byte *data) {
	boolean valid = false;
	Configuration * cfg = malloc(sizeof(Configuration));

	json_object *config = json_tokener_parse(data);
	if (config != NULL) {
		char * tmp;

		if (json_object_get_type(config) == json_type_object) {
			valid = true;
			json_object * value = json_object_object_get(config, "version");
			if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
				tmp = json_object_get_string(value);
				int length = strlen(tmp);
				cfg->version = malloc(length + 1);
				if (cfg->version == NULL) {
					exit(-1);
				}
				memcpy(cfg->version, tmp, length + 1);
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

