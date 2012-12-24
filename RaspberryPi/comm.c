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

#define SERVICES_COUNT 1

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

typedef struct {
	char method[METHOD_SIZE];
	char path[PATH_SIZE];
} Request;

typedef struct {
	char method[METHOD_SIZE];
	int statusCode;
	char status[STATUS_SIZE];
	char contentType[CONTENT_TYPE_SIZE];
	int dataSize;
	char data[BUFFER_SIZE];
} Response;

typedef struct {
	char * path;
	void (*handleRequest)(Request * request, Response * response);
} HttpService;

HttpService services[SERVICES_COUNT];

void handleVersionRequest(Request * request, Response * response) {
	sprintf(response->contentType, "text/json");
	sprintf(response->data, "{\"version\":\"1.0\"}\r\n");
	response->dataSize = strlen(response->data);

}

void setupComm() {
	services[0].path = "/cmd/version";
	services[0].handleRequest = handleVersionRequest;
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
			response->dataSize, response->contentType);
	send(clntSocket, buffer, strlen(buffer), MSG_NOSIGNAL | MSG_DONTWAIT);
	send(clntSocket, response->data, response->dataSize,
			MSG_NOSIGNAL | MSG_DONTWAIT);
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

		// Double newline
		if (lastChar == '\n' && curChar == '\r') {
			curChar = 0;
			continue;
		}
		if (lastChar == '\r' && curChar == '\n') {
			curChar = 0;
			continue;
		}

		if (curChar == '\r' || curChar == '\n') {
			buffer[bufferOffset] = 0;
			printf("  READ LINE: %s\n", buffer);

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
			}

			if (strlen(buffer) == 0) {
				// Empty Line.  End of header.

				printf("  END OF HEADER\n");

				bool handled = false;

				sprintf(response->method, "HTTP/1.1");
				sprintf(response->status, "OK");
				sprintf(response->contentType, "text/html");
				response->statusCode = 200;
				response->dataSize = 0;

				for (int i = 0; i < SERVICES_COUNT; i++) {

					if (strcmp(request->path, services[i].path) == 0) {
						services[i].handleRequest(request, response);
						handled = true;
						break;
					}

				}

				if (!handled) {
					sprintf(response->status, "Not Found");
					response->statusCode = 404;
					sprintf(response->data, "Not Found");
					response->dataSize = strlen(response->data);
				}
				sendHttpResponse(clntSocket, response);
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
	int connected;
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
