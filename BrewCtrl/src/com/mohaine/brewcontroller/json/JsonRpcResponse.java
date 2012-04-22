package com.mohaine.brewcontroller.json;

import java.util.ArrayList;
import java.util.List;

public class JsonRpcResponse {
	String id;
	Object error;
	Object result;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getError() {
		return error;
	}

	public void setError(Object error) {
		this.error = error;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public static class JsonRpcResponseHandler extends JsonObjectHandler<JsonRpcResponse> {
		@Override
		public Class<? extends JsonRpcResponse> getObjectType() {
			return JsonRpcResponse.class;
		}

		@Override
		public List<JsonObjectPropertyHandler<JsonRpcResponse, ?>> getPropertyHandlers() {
			ArrayList<JsonObjectPropertyHandler<JsonRpcResponse, ?>> phs = new ArrayList<JsonObjectPropertyHandler<JsonRpcResponse, ?>>();

			phs.add(new JsonObjectPropertyHandler<JsonRpcResponse, String>() {
				@Override
				public String getName() {
					return "id";
				}

				@Override
				public String getValue(JsonRpcResponse object) {
					return object.getId();
				}

				@Override
				public void setValue(JsonRpcResponse object, String value) {
					object.setId(value);
				}
			});
			phs.add(new JsonObjectPropertyHandler<JsonRpcResponse, Object>() {
				@Override
				public String getName() {
					return "error";
				}

				@Override
				public Object getValue(JsonRpcResponse object) {
					return object.getError();
				}

				@Override
				public void setValue(JsonRpcResponse object, Object value) {
					object.setError(value);
				}

			});
			phs.add(new JsonObjectPropertyHandler<JsonRpcResponse, Object>() {
				@Override
				public String getName() {
					return "result";
				}

				@Override
				public Object getValue(JsonRpcResponse object) {
					return object.getResult();
				}

				@Override
				public void setValue(JsonRpcResponse object, Object value) {
					object.setResult(value);
				}
			});
			return phs;
		}

		@Override
		public String getType() {
			return "JsonRpcResponse";
		}
	}

}
