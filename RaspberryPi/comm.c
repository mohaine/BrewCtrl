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

#define ONE_WIRE_PIN 4
#define DEFAULT_PORT 2739

#define BUFFER_SIZE 1024*20
#define PATH_SIZE 1024
#define METHOD_SIZE 10
#define STATUS_SIZE 30
#define CONTENT_TYPE_SIZE 30

#define SERVICES_COUNT 3

#include "sensor.h"
#include "comm.h"
#include "crc8.h"
#include "control.h"

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

char LAYOUT[BUFFER_SIZE];
int LAYOUT_SIZE;

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

		if (paramData[index + nameLenght] == '='
				&& strncmp(name, paramData + index, nameLenght) == 0) {

			index = index + nameLenght + 1;

			int length = 0;

			while (index < paramDataLength && paramData[index] != '\r'
					&& paramData[index] != '\n') {

				if (paramData[index] == '%' && index < paramDataLength + 2) {
					index++;
					unsigned int data;

					sscanf(paramData + index, "%02x", &data);
					dest[length] = (char) data;
					index++;
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
			while (index < paramDataLength && paramData[index] != '\r'
					&& paramData[index] != '\n') {
				index++;
			}
// Go past the EOL
			while (index < paramDataLength
					&& (paramData[index] == '\r' || paramData[index] == '\n')) {
				index++;
			}
		}

	}

	return paramSize;
}

void handleLayoutRequest(Request * request, Response * response) {

	if (request->contentLength > 0) {
		int paramSize = readParam("layout", request->content,
				request->contentLength, LAYOUT);
		if (paramSize > 0) {
			LAYOUT_SIZE = paramSize;
		} else {
			response->statusCode = 400;
			sprintf(response->status, "Bad Request");
			sprintf(response->content, "Bad Request");
			return;
		}
	}

	sprintf(response->contentType, "text/json");
	memcpy(response->content, LAYOUT, LAYOUT_SIZE);
	response->contentLength = LAYOUT_SIZE;
}
void handleStatusRequest(Request * request, Response * response) {
	char tmp[BUFFER_SIZE];

	if (request->contentLength > 0) {
		int paramSize = readParam("mode", request->content,
				request->contentLength, &tmp);
		if (paramSize > 0) {
			if (strcmp(tmp, "OFF") == 0) {
				getControl()->mode = MODE_OFF;
				turnOff();
			} else if (strcmp(tmp, "ON") == 0) {
				getControl()->mode = MODE_ON;
			} else if (strcmp(tmp, "HOLD") == 0) {
				getControl()->mode = MODE_HOLD;
			} else {
				response->statusCode = 400;
				sprintf(response->status, "Bad Request");
				sprintf(response->content, "Bad Request");
				return;
			}
		}
	}

//	if (Mode.OFF.toString().equals(modeParam)) {
//		status.setMode(Mode.OFF);
//	} else if (Mode.ON.toString().equals(modeParam)) {
//		status.setMode(Mode.ON);
//	} else if (Mode.HOLD.toString().equals(modeParam)) {
//		status.setMode(Mode.HOLD);
//	}

	sprintf(response->contentType, "text/json");

	json_object *status, *sensor, *sensors, *steps;

	status = json_object_new_object();

	if (getControl()->mode == MODE_OFF) {
		json_object_object_add(status, "mode", json_object_new_string("OFF"));
	} else if (getControl()->mode == MODE_ON) {
		json_object_object_add(status, "mode", json_object_new_string("ON"));
	} else if (getControl()->mode == MODE_HOLD) {
		json_object_object_add(status, "mode", json_object_new_string("HOLD"));
	}

	sensors = json_object_new_array();
	json_object_object_add(status, "sensors", sensors);
	steps = json_object_new_array();
	json_object_object_add(status, "steps", steps);

	for (int i = 0; i < getSensorCount(); i++) {
		TempSensor *ts = getSensorByIndex(i);

		sprintf(&tmp, "%02x%02x%02x%02x%02x%02x%02x%02x", ts->address[0],
				ts->address[1], ts->address[2], ts->address[3],
				ts->address[4], ts->address[5], ts->address[6],
				ts->address[7]);

		sensor = json_object_new_object();
		json_object_object_add(sensor, "address", json_object_new_string(tmp));

		json_object_object_add(sensor, "tempatureC",
				json_object_new_double(ts->lastTemp));
		json_object_object_add(sensor, "reading",
				json_object_new_boolean(ts->reading));
		json_object_array_add(sensors, sensor);
	}

	sprintf(&response->content, "%s", json_object_get_string(status));
	response->contentLength = strlen(response->content);
}

void setupComm() {

	LAYOUT_SIZE = 0;
	services[0].path = "/cmd/status";
	services[0].handleRequest = handleStatusRequest;

	services[1].path = "/cmd/layout";
	services[1].handleRequest = handleLayoutRequest;

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

	sprintf(buffer,
			"%s %d %s\r\nContent-Length: %d\r\nContent-Type: %s\r\n\r\n",
			response->method, response->statusCode, response->status,
			response->contentLength, response->contentType);
	send(clntSocket, buffer, strlen(buffer), MSG_NOSIGNAL | MSG_DONTWAIT);
	send(clntSocket, response->content, response->contentLength,
			MSG_NOSIGNAL | MSG_DONTWAIT);

//	write(stdout, response->content, response->contentLength);

	free(buffer);
}

void* handleClientThread(void *ptr) {

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

				if (strncmp("Content-Length: ", buffer,
						strlen("Content-Length: ")) == 0) {
					sscanf(buffer + strlen("Content-Length: "), "%d",
							&request->contentLength);
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
						response->statusCode = 400;
						sprintf(response->status, "Bad Request");
						sprintf(response->content, "Bad Request");
						response->contentLength = strlen(response->content);
					} else {

						for (int i = 0; i < request->contentLength; i++) {
							request->content[i] = 0;
						}

						readSize = recv(clntSocket, request->content,
								request->contentLength, 0);
						if (readSize == 0) {
							break;
						}
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
					sprintf(response->status, "Not Found");
					response->statusCode = 404;
					sprintf(response->content, "Not Found");
					response->contentLength = strlen(response->content);
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

	return NULL;
}

void* listenThread(void *ptr) {

	int sock;
	int setSockOp = 1;
	pthread_t thread;

	struct sockaddr_in server_addr, client_addr;
	socklen_t sin_size;

	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
		perror("Socket");
		return NULL;
	}

	if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &setSockOp, sizeof(int))
			== -1) {
		perror("Setsockopt");
		return NULL;
	}

	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(DEFAULT_PORT);
	server_addr.sin_addr.s_addr = INADDR_ANY;
	bzero(&(server_addr.sin_zero), 8);

	if (bind(sock, (struct sockaddr *) &server_addr, sizeof(struct sockaddr))
			== -1) {
		perror("Unable to bind");
		return NULL;
	}

	if (listen(sock, 5) == -1) {
		perror("Listen");
		return NULL;
	}

	printf("Waiting for client on port %d\n", DEFAULT_PORT);
	fflush(stdout);

	while (1) {
		sin_size = sizeof(struct sockaddr_in);

		HandleClientParmas * params = malloc(sizeof(HandleClientParmas));

		params->clntSocket = accept(sock, (struct sockaddr *) &client_addr,
				&sin_size);
		printf("Connection from (%s , %d)\n", inet_ntoa(client_addr.sin_addr),
				ntohs(client_addr.sin_port));

		pthread_create(&thread, NULL, handleClientThread, (void*) params);
	}

	return NULL;
}
void startComm() {
	setupComm();

	pthread_t thread;
	pthread_create(&thread, NULL, listenThread, NULL);

}
