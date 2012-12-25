/* Hello World program */

#include <stdio.h>
#include <unistd.h>

#include "duty.h"
#include "loop.h"
#include "brewctrl.h"
#include "comm.h"
#include "json.h"

int main() {
//	json_tokener *tok;
//	json_object *my_string, *my_int, *my_object, *my_array;
//	json_object *new_obj;
//	int i;
//
//	new_obj = json_tokener_parse("{ \"foo");
//	if (is_error(new_obj))
//		printf("got error as expected\n");
//
//	new_obj =
//			json_tokener_parse(
//					"{ \"foo\": \"bar\",\"fooInt\": 123 ,\"fooDouble\": 123.456 ,\"fooBool\": true }");
//	printf("new_obj.to_string()=%s\n", json_object_to_json_string(new_obj));
//
//	enum json_type type;
//	json_object_object_foreach(new_obj, key, val) {
//		printf("type: ", type);
//		type = json_object_get_type(val);
//		switch (type) {
//		case json_type_null:
//			printf("json_type_nulln");
//			break;
//		case json_type_boolean:
//			printf("json_type_booleann");
//			printf(" value: %d", json_object_get_boolean(val));
//			break;
//		case json_type_double:
//			printf("json_type_doublen");
//			printf(" value: %f", json_object_get_double(val));
//			break;
//		case json_type_int:
//			printf("json_type_intn");
//			printf(" value: %d", json_object_get_int(val));
//			break;
//		case json_type_object:
//			printf("json_type_objectn");
//			break;
//		case json_type_array:
//			printf("json_type_arrayn");
//			break;
//		case json_type_string:
//			printf("json_type_stringn");
//			printf(" value: %s", json_object_get_string(val));
//			break;
//		}
//		printf("\n");
//	}
//
//	json_object_put(new_obj);

	printf("Start Comm\n");
	startComm();
	printf("Start Loop\n");
	loop();

}

