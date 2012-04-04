package com.mohaine.brewcontroller.json;

import java.util.ArrayList;
import java.util.List;

public class JsonRpcRequest {
	String id;
	String method;
	List<Object> params;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

	public void setParams(Object... oparams) {
		this.params = new ArrayList<Object>();
		for (Object object : oparams) {
			params.add(object);
		}
	}

	public static class JsonRpcRequestHandler extends JsonObjectHandler<JsonRpcRequest> {
		@Override
		public Class<? extends JsonRpcRequest> getObjectType() {
			return JsonRpcRequest.class;
		}

		@Override
		public List<JsonObjectPropertyHandler<JsonRpcRequest, ?>> getPropertyHandlers() {
			ArrayList<JsonObjectPropertyHandler<JsonRpcRequest, ?>> phs = new ArrayList<JsonObjectPropertyHandler<JsonRpcRequest, ?>>();

			phs.add(new JsonObjectPropertyHandler<JsonRpcRequest, String>() {
				@Override
				public String getName() {
					return "id";
				}

				@Override
				public String getValue(JsonRpcRequest object) {
					return object.getId();
				}

				@Override
				public void setValue(JsonRpcRequest object, String value) {
					object.setId(value);
				}
			});
			phs.add(new JsonObjectPropertyHandler<JsonRpcRequest, String>() {
				@Override
				public String getName() {
					return "method";
				}

				@Override
				public String getValue(JsonRpcRequest object) {
					return object.getMethod();
				}

				@Override
				public void setValue(JsonRpcRequest object, String value) {
					object.setMethod(value);
				}
			});

			phs.add(new JsonObjectPropertyHandler<JsonRpcRequest, List<Object>>() {
				@Override
				public String getName() {
					return "params";
				}

				@Override
				public List<Object> getValue(JsonRpcRequest object) {
					return object.getParams();
				}

				@Override
				public void setValue(JsonRpcRequest object, List<Object> value) {
					object.setParams(value);
				}
			});
			return phs;
		}

		@Override
		public String getType() {
			return "JsonRpcRequest";
		}
	}

}
