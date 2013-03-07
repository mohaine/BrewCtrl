/*
 Copyright 2009-2013 Michael Graessle
 
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

#define ONE_WIRE_PIN 4
#define DEFAULT_PORT 2739

#define BUFFER_SIZE 1024*512
#define PATH_SIZE 1024
#define METHOD_SIZE 10
#define STATUS_SIZE 30
#define CONTENT_TYPE_SIZE 30

#define SERVICES_COUNT 3
#define LAYOUT_FILE "BrewControllerConfig.json"

#include "sensor.h"
#include "comm.h"
#include "config.h"
#include "logger.h"
#include "control.h"

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

typedef struct {
	char method[METHOD_SIZE];
	char path[PATH_SIZE];
	int contentLength;
	char content[BUFFER_SIZE];
} Request;

typedef struct {
	char method[METHOD_SIZE];
	int statusCode;
	char status[STATUS_SIZE];
	char contentType[CONTENT_TYPE_SIZE];
	int contentLength;
	char content[BUFFER_SIZE];
} Response;

typedef struct {
	char * path;
	void (*handleRequest)(Request * request, Response * response);
} HttpService;

HttpService services[SERVICES_COUNT];

int checkSuffix(const char *str, const char *suffix) {
	if (!str || !suffix)
		return 0;
	size_t lenstr = strlen(str);
	size_t lensuffix = strlen(suffix);
	if (lensuffix > lenstr)
		return 0;
	return strncmp(str + lenstr - lensuffix, suffix, lensuffix) == 0;
}

void handleOtherRequest(Request * request, Response * response) {
	DBG("Request Path: '%s'\n", request->path);

	char fileName[PATH_SIZE];
	int length = strlen(request->path);

	if (length > PATH_SIZE) {
		length = PATH_MAX;
	}
	int offset = 0;
	fileName[offset++] = 'w';
	fileName[offset++] = 'e';
	fileName[offset++] = 'b';
	fileName[offset++] = '/';

	if (length == 1) {
		fileName[offset++] = 'W';
		fileName[offset++] = 'e';
		fileName[offset++] = 'b';
		fileName[offset++] = 'C';
		fileName[offset++] = 't';
		fileName[offset++] = 'r';
		fileName[offset++] = 'l';
		fileName[offset++] = '.';
		fileName[offset++] = 'h';
		fileName[offset++] = 't';
		fileName[offset++] = 'm';
		fileName[offset++] = 'l';
	} else {
		for (int i = 1; i < length; i++) {
			char charAt = request->path[i];
			fileName[offset++] = charAt;
			//TODO CLEAN
		}
	}
	fileName[offset] = 0;

	DBG("Map to File %s\n", fileName);

	struct stat st;
	if (stat(fileName, &st) >= 0) {
		DBG("Send File %s\n", fileName);

		ssize_t s = st.st_size;
		if (s > BUFFER_SIZE) {
			s = BUFFER_SIZE;
		}
		FILE* f = fopen(fileName, "rb");
		if (f) {
			int readSize = fread(response->content, 1, s, f);
			if (readSize == s) {

				if (checkSuffix(fileName, ".html")) {
					sprintf(response->contentType, "text/html");
				} else if (checkSuffix(fileName, ".js")) {
					sprintf(response->contentType, "text/javascript");
				} else if (checkSuffix(fileName, ".css")) {
					sprintf(response->contentType, "text/css");
				} else if (checkSuffix(fileName, ".gif")) {
					sprintf(response->contentType, "image/gif");
				} else if (checkSuffix(fileName, ".png")) {
					sprintf(response->contentType, "image/png");
				} else if (checkSuffix(fileName, ".ico")) {
					sprintf(response->contentType, "image/icon");
				} else {
					DBG("Unhandled file type %s\n", fileName);

				}
				response->contentLength = s;
			}
			fclose(f);
		}

	} else {
		DBG("404 File %s\n", fileName);

		sprintf(response->status, "Not Found");
		response->statusCode = 404;
		sprintf(response->content, "Not Found");
		response->contentLength = strlen(response->content);
	}
}

void handleVersionRequest(Request * request, Response * response) {
	sprintf(response->contentType, "text/json");
	sprintf(response->content, "{\"version\":\"1.0\"}\r\n");
	response->contentLength = strlen(response->content);
}

int readParam(char* name, char* paramData, int paramDataLength, char* dest) {
	int paramSize = -1;
	int nameLenght = strlen(name);

	int index = 0;

	while (index < paramDataLength) {

		if (paramData[index + nameLenght] == '=' && strncmp(name, paramData + index, nameLenght) == 0) {
			index = index + nameLenght + 1;
			int length = 0;
			while (index < paramDataLength && paramData[index] != '\r' && paramData[index] != '\n') {
				if (paramData[index] == '%' && index < paramDataLength + 2) {
					index++;
					unsigned int data;
					data = 0;
					sscanf(paramData + index, "%02x", &data);
					dest[length] = (char) data;
					index++;
				} else if (paramData[index] == '+') {
					dest[length] = ' ';
				} else {
					dest[length] = paramData[index];
				}
				length++;
				index++;
			}
			dest[length] = 0;

			return length;

		} else {
// Find the EOL
			while (index < paramDataLength && paramData[index] != '\r' && paramData[index] != '\n') {
				index++;
			}
// Go past the EOL
			while (index < paramDataLength && (paramData[index] == '\r' || paramData[index] == '\n')) {
				index++;
			}
		}

	}

	return paramSize;
}

void handleConfigRequest(Request * request, Response * response) {
	if (request->contentLength > 0) {
		char * buffer = malloc(request->contentLength + 1);
		int paramSize = readParam("configuration", request->content, request->contentLength, buffer);
		if (paramSize > 0) {
			Configuration * cfg = parseJsonConfiguration(buffer);
			if (cfg != NULL) {
				if (cfg->version != NULL) {
					free(cfg->version);
				}
				cfg->version = generateRandomId();
				turnOff();
				setConfiguration(cfg);
				writeConfiguration(cfg);
			}

		} else {
			DBG("400 for handleConfigRequest\n");
			response->statusCode = 400;
			sprintf(response->status, "Bad Request");
			sprintf(response->content, "Bad Request");
			return;
		}
		free(buffer);
	}

	Configuration* configuration = getConfiguration();
	if (configuration != NULL) {
		char * json = formatJsonConfiguration(configuration);
		sprintf(response->contentType, "text/json");
		sprintf(response->content, "%s", json);
		response->contentLength = strlen(response->content);
		free(json);
	} else {
		sprintf(response->contentType, "text/json");
		response->contentLength = 0;
	}

}

bool parseJsonStep(json_object *step, ControlStep * cs) {
	DBG("Parse Step\n");

	bool valid = true;
	json_object * value = json_object_object_get(step, "id");
	if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
		sprintf(cs->id, "%s", json_object_get_string(value));
	} else {

		DBG("Invalid Step: No Id\n");

		valid = false;
	}

	value = json_object_object_get(step, "name");
	if (valid && value != NULL && json_object_get_type(value) == json_type_string) {
		sprintf(cs->name, "%s", json_object_get_string(value));
	} else {
		DBG("Invalid Step: No Name\n");
		valid = false;
	}

	value = json_object_object_get(step, "stepTime");
	if (valid && value != NULL && json_object_get_type(value) == json_type_int) {
		cs->stepTime = json_object_get_int(value);
	} else {
		DBG("Invalid Step: No Step Time\n");
		valid = false;
	}

	value = json_object_object_get(step, "active");
	if (valid && value != NULL && json_object_get_type(value) == json_type_boolean) {
		cs->active = json_object_get_boolean(value);
	} else {
		cs->active = false;
	}

	if (valid) {
		DBG("Parse Step %s\n", cs->name);

		json_object * controlPoints = json_object_object_get(step, "controlPoints");

		if (controlPoints != NULL && json_object_get_type(controlPoints) == json_type_array) {
			int controlPointCount = json_object_array_length(controlPoints);

			if (controlPointCount > MAX_CP_COUNT) {
				controlPointCount = MAX_CP_COUNT;
			}
			cs->controlPointCount = controlPointCount;
			for (int cpI = 0; valid && cpI < controlPointCount; cpI++) {

				json_object *controlPoint = json_object_array_get_idx(controlPoints, cpI);
				ControlPoint *cp = &cs->controlPoints[cpI];

				value = json_object_object_get(controlPoint, "controlPin");
				if (value != NULL && json_object_get_type(value) == json_type_int) {
					cp->controlPin = json_object_get_int(value);
				} else {
					DBG("Invalid Control Point: No controlPin\n");
					valid = false;
					break;
				}

				value = json_object_object_get(controlPoint, "duty");
				if (value != NULL && json_object_get_type(value) == json_type_int) {
					cp->duty = json_object_get_int(value);
				} else {
					DBG("Invalid Control Point: No duty\n");
					valid = false;
					break;
				}

				value = json_object_object_get(controlPoint, "fullOnAmps");
				if (value != NULL && json_object_get_type(value) == json_type_int) {
					cp->fullOnAmps = json_object_get_int(value);
				} else {
					DBG("Invalid Control Point: No fullOnAmps\n");
					valid = false;
					break;
				}

				value = json_object_object_get(controlPoint, "tempSensorAddress");
				if (value != NULL && json_object_get_type(value) == json_type_string) {
					sprintf(cp->tempSensorAddressPtr, "%s", json_object_get_string(value));
				} else {
					DBG("Invalid Control Point: No tempSensorAddress\n");
					valid = false;
					break;
				}

				value = json_object_object_get(controlPoint, "targetTemp");
				if (value != NULL && json_object_get_type(value) == json_type_double) {
					cp->targetTemp = json_object_get_double(value);
				} else if (value != NULL && json_object_get_type(value) == json_type_int) {
					cp->targetTemp = json_object_get_int(value);
				} else {
					DBG("Invalid Control Point: No targetTemp\n");
					valid = false;
					break;
				}

				value = json_object_object_get(controlPoint, "hasDuty");
				if (value != NULL && json_object_get_type(value) == json_type_boolean) {
					cp->hasDuty = json_object_get_boolean(value);
				} else {
					DBG("Invalid Control Point: No hasDuty\n");
					valid = false;
					break;
				}

				value = json_object_object_get(controlPoint, "automaticControl");
				if (value != NULL && json_object_get_type(value) == json_type_boolean) {
					cp->automaticControl = json_object_get_boolean(value);
				} else {
					DBG("Invalid Control Point: No automaticControl\n");
					valid = false;
					break;
				}

			}

		} else {
			valid = false;
		}
	}

	return valid;
}

void handleStatusRequest(Request * request, Response * response) {
	char tmp[BUFFER_SIZE];

	if (request->contentLength > 0) {

		bool valid = true;

		int paramSize = readParam("mode", request->content, request->contentLength, tmp);
		if (valid && paramSize > 0) {
			DBG("handleStatusRequest: mode\n");
			if (strcmp(tmp, "OFF") == 0) {
				getControl()->mode = MODE_OFF;
				turnOff();
			} else if (strcmp(tmp, "ON") == 0) {
				getControl()->mode = MODE_ON;
			} else if (strcmp(tmp, "HOLD") == 0) {
				getControl()->mode = MODE_HOLD;
			} else if (strcmp(tmp, "HEAT_OFF") == 0) {
				getControl()->mode = MODE_HEAT_OFF;
				turnHeatOff();
			} else {
				valid = false;
			}
		}
		paramSize = readParam("steps", request->content, request->contentLength, tmp);

		if (valid && paramSize > 0) {

			DBG("handleStatusRequest: steps\n");

			json_object *steps = json_tokener_parse(tmp);

			if (steps == NULL || json_object_get_type(steps) != json_type_array) {
				valid = false;
			} else {
				int stepCount = json_object_array_length(steps);
				if (stepCount > MAX_STEP_COUNT) {
					stepCount = MAX_STEP_COUNT;
				}

				lockSteps();

				for (int i = 0; valid && i < stepCount; i++) {
					json_object *step = json_object_array_get_idx(steps, i);
					ControlStep *cs = getControlStep(i);
					cs->active = false;
					valid = parseJsonStep(step, cs);
					//Init Steps
					if (valid) {
						for (int cpI = 0; valid && cpI < cs->controlPointCount; cpI++) {
							ControlPoint *cp = &cs->controlPoints[cpI];
							cp->initComplete = false;
						}
					} else {
						DBG("Invalid Step\n");
					}
				}
				if (valid) {
					setControlStepCount(stepCount);
				} else {
					setControlStepCount(0);
				}
				unlockSteps();
			}

			if (steps != NULL) {
				json_object_put(steps);
			}
		}

		paramSize = readParam("modifySteps", request->content, request->contentLength, tmp);
		if (valid && paramSize > 0) {

			DBG("handleStatusRequest: modifySteps\n");

			json_object *steps = json_tokener_parse(tmp);

			if (steps == NULL || json_object_get_type(steps) != json_type_array) {
				valid = false;
			} else {
				int stepCount = json_object_array_length(steps);
				lockSteps();
				for (int i = 0; valid && i < stepCount; i++) {
					json_object *step = json_object_array_get_idx(steps, i);

					json_object * value = json_object_object_get(step, "id");

					if (value != NULL) {
						for (int j = 0; valid && j < getControlStepCount(); j++) {
							ControlStep *cs = getControlStep(j);
							if (strcmp(json_object_get_string(value), cs->id) == 0) {
								printf("Update step %s\n", cs->name);
								valid = parseJsonStep(step, cs);
								break;
							}
						}
					} else {
						valid = false;
					}
				}
				unlockSteps();
			}

			if (steps != NULL) {
				json_object_put(steps);
			}
		}
		if (!valid) {
			DBG("400 for handleStatusRequest\n");
			response->statusCode = 400;
			sprintf(response->status, "Bad Request");
			sprintf(response->content, "Bad Request");
			return;
		}
	}

	sprintf(response->contentType, "text/json");

	json_object *status, *sensor, *sensors, *steps, *step, *controlPoints, *controlPoint;

	status = json_object_new_object();

	Configuration* config = getConfiguration();
	if (config != NULL && config->version != NULL) {
		json_object_object_add(status, "configurationVersion", json_object_new_string(config->version));
	}

	if (getControl()->mode == MODE_OFF) {
		json_object_object_add(status, "mode", json_object_new_string("OFF"));
	} else if (getControl()->mode == MODE_ON) {
		json_object_object_add(status, "mode", json_object_new_string("ON"));
	} else if (getControl()->mode == MODE_HOLD) {
		json_object_object_add(status, "mode", json_object_new_string("HOLD"));
	} else if (getControl()->mode == MODE_HEAT_OFF) {
		json_object_object_add(status, "mode", json_object_new_string("HEAT_OFF"));
	}

	sensors = json_object_new_array();
	json_object_object_add(status, "sensors", sensors);
	steps = json_object_new_array();
	json_object_object_add(status, "steps", steps);

	lockSteps();
	for (int i = 0; i < getControlStepCount(); i++) {
		ControlStep *cs = getControlStep(i);

		step = json_object_new_object();
		json_object_array_add(steps, step);

		controlPoints = json_object_new_array();

		json_object_object_add(step, "name", json_object_new_string(cs->name));
		json_object_object_add(step, "id", json_object_new_string(cs->id));
		json_object_object_add(step, "stepTime", json_object_new_int(cs->stepTime));
		json_object_object_add(step, "active", json_object_new_boolean(cs->active));

		json_object_object_add(step, "controlPoints", controlPoints);
		for (int cpI = 0; cpI < cs->controlPointCount; cpI++) {
			ControlPoint * cp = &cs->controlPoints[cpI];
			controlPoint = json_object_new_object();
			json_object_array_add(controlPoints, controlPoint);

			json_object_object_add(controlPoint, "controlPin", json_object_new_int(cp->controlPin));
			json_object_object_add(controlPoint, "duty", json_object_new_int(cp->duty));
			json_object_object_add(controlPoint, "fullOnAmps", json_object_new_int(cp->fullOnAmps));
			json_object_object_add(controlPoint, "tempSensorAddress", json_object_new_string(cp->tempSensorAddressPtr));
			json_object_object_add(controlPoint, "targetTemp", json_object_new_double(cp->targetTemp));
			json_object_object_add(controlPoint, "hasDuty", json_object_new_boolean(cp->hasDuty));
			json_object_object_add(controlPoint, "automaticControl", json_object_new_boolean(cp->automaticControl));
		}

	}
	unlockSteps();

	for (int i = 0; i < getSensorCount(); i++) {
		TempSensor *ts = getSensorByIndex(i);

		sensor = json_object_new_object();
		json_object_array_add(sensors, sensor);

		json_object_object_add(sensor, "address", json_object_new_string(ts->addressPtr));

		json_object_object_add(sensor, "tempatureC", json_object_new_double(ts->lastTemp));
		json_object_object_add(sensor, "reading", json_object_new_boolean(hasVaildTemp(ts)));
	}

	sprintf(response->content, "%s", json_object_get_string(status));
	json_object_put(status);

	response->contentLength = strlen(response->content);

}

void setupComm() {

	services[0].path = "/cmd/status";
	services[0].handleRequest = handleStatusRequest;

	services[1].path = "/cmd/configuration";
	services[1].handleRequest = handleConfigRequest;

	services[2].path = "/cmd/version";
	services[2].handleRequest = handleVersionRequest;

}

long lastCommTime = 0;

long lastControlIdTime() {
	return lastCommTime;
}

typedef struct {
	int clntSocket;
} HandleClientParmas;

void sendHttpResponse(int clntSocket, Response *response) {
	char * buffer = malloc(BUFFER_SIZE);

	sprintf(buffer, "%s %d %s\r\nContent-Length: %d\r\nContent-Type: %s\r\n\r\n", response->method, response->statusCode, response->status, response->contentLength, response->contentType);
	send(clntSocket, buffer, strlen(buffer), MSG_NOSIGNAL | MSG_DONTWAIT);
	send(clntSocket, response->content, response->contentLength, MSG_NOSIGNAL | MSG_DONTWAIT);

//	write(stdout, response->content, response->contentLength);

	free(buffer);
}

void handleClientThread(void *ptr) {

	HandleClientParmas * params = (HandleClientParmas *) ptr;
	int clntSocket = params->clntSocket;
	free(params);

	char * buffer = malloc(BUFFER_SIZE);
	Request * request = malloc(sizeof(Request));
	Response * response = malloc(sizeof(Response));
	request->method[0] = 0;
	request->path[0] = 0;
	request->contentLength = 0;

	int bufferOffset = 0;

	int headerLine = 0;
	char lastChar = 0;
	char curChar = 0;

	while (1) {
		ssize_t readSize;

		readSize = recv(clntSocket, buffer + bufferOffset, 1, 0);
		if (readSize == 0) {
			break;
		}

		lastChar = curChar;
		curChar = buffer[bufferOffset];

		if (curChar == '\r') {
			continue;
		}

		if (lastChar == '\r' && curChar == '\n') {
			buffer[bufferOffset] = 0;
//			printf("  READ LINE: %s\n", buffer);

			if (headerLine == 0) {
				// Parse Header
				int index = 0;
				for (int i = 0; i < METHOD_SIZE; i++) {
					request->method[i] = buffer[i + index];
					if (request->method[i] == ' ' || request->method[i] == 0) {
						request->method[i] = 0;
						index = index + i;
						break;
					}
				}
				if (index > 0) {
					for (; index < bufferOffset; index++) {
						if (buffer[index] != ' ') {
							break;
						}
					}
					for (int i = 0; i < PATH_SIZE; i++) {
						request->path[i] = buffer[i + index];
						if (request->path[i] == ' ' || request->path[i] == 0) {
							request->path[i] = 0;
							index = index + i;
							break;
						}
					}

				}
			} else {

				if (strncmp("Content-Length: ", buffer, strlen("Content-Length: ")) == 0) {
					sscanf(buffer + strlen("Content-Length: "), "%d", &request->contentLength);
				}

			}

			if (strlen(buffer) == 0) {
				// Empty Line.  End of header.
				bool handled = false;

				sprintf(response->method, "HTTP/1.1");
				sprintf(response->status, "OK");
				sprintf(response->contentType, "text/html");
				response->statusCode = 200;
				response->contentLength = 0;

				if (request->contentLength > 0) {

					if (request->contentLength > BUFFER_SIZE) {
						handled = true;
						DBG("400 for Post Size %d\n",request->contentLength);
						response->statusCode = 400;
						sprintf(response->status, "Bad Request");
						sprintf(response->content, "Bad Request");
						response->contentLength = strlen(response->content);
					} else {

						for (int i = 0; i < BUFFER_SIZE; i++) {
							request->content[i] = 0;
						}

						DBG("Read: %d \n",request->contentLength);

						readSize = recv(clntSocket, request->content, request->contentLength, MSG_WAITALL);

						DBG("Actual Read: %d\n",readSize);

						if (readSize == 0) {
							break;
						}
						request->content[readSize + 1] = 0;
						DBG("Data: %s\n",request->content);

					}

				}

				if (!handled) {
					for (int i = 0; i < SERVICES_COUNT; i++) {
						if (strcmp(request->path, services[i].path) == 0) {
							services[i].handleRequest(request, response);
							handled = true;
							break;
						}
					}
				}

				if (!handled) {
					handleOtherRequest(request, response);
				}
				sendHttpResponse(clntSocket, response);

				headerLine = 0;
				bufferOffset = 0;
				request->method[0] = 0;
				request->path[0] = 0;
				request->contentLength = 0;

				continue;
			}

			headerLine++;
			bufferOffset = 0;
		} else {
			bufferOffset += readSize;
		}
	}

	close(clntSocket);
	fflush(stdout);
	free(request);
	free(response);
	free(buffer);

	pthread_exit(NULL);
}

void listenThread(void *ptr) {

	int sock;
	int setSockOp = 1;
	pthread_t thread;

	struct sockaddr_in server_addr, client_addr;
	socklen_t sin_size;

	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
		perror("Socket");
		pthread_exit(NULL);
	}

	if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &setSockOp, sizeof(int)) == -1) {
		perror("Setsockopt");
		pthread_exit(NULL);
	}

	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(DEFAULT_PORT);
	server_addr.sin_addr.s_addr = INADDR_ANY;
	bzero(&(server_addr.sin_zero), 8);

	if (bind(sock, (struct sockaddr *) &server_addr, sizeof(struct sockaddr)) == -1) {
		perror("Unable to bind");
		pthread_exit(NULL);
	}

	if (listen(sock, 5) == -1) {
		perror("Listen");
		pthread_exit(NULL);
	}

	printf("Waiting for client on port %d\n", DEFAULT_PORT);
	fflush(stdout);

	while (1) {
		sin_size = sizeof(struct sockaddr_in);

		HandleClientParmas * params = malloc(sizeof(HandleClientParmas));

		params->clntSocket = accept(sock, (struct sockaddr *) &client_addr, &sin_size);
		printf("Connection from (%s , %d)\n", inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));

		pthread_create(&thread, NULL, handleClientThread, (void*) params);
	}

	pthread_exit(NULL);
}
void startComm() {
	setupComm();

	pthread_t thread;
	pthread_create(&thread, NULL, listenThread, NULL);

}
