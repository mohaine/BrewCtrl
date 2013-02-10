package com.mohaine.brewcontroller.web.server;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mohaine.brewcontroller.ControllerUrlLoader;
import com.mohaine.brewcontroller.net.URLRequest;
import com.mohaine.brewcontroller.server.util.StreamUtils;

public class MockServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		URL url = new URL(getBaseCmdUrl() + request.getPathInfo());

		URLRequest r = new URLRequest(url);

		int contentLength = request.getContentLength();
		if (contentLength > 0) {
			byte[] postData = StreamUtils.readStream(request.getInputStream(), contentLength);
			r.setPostData(postData);
		}

		String string = r.getString();
		response.setContentType(r.getContentType());
		byte[] bytes = string.getBytes();
		response.setContentLength(bytes.length);
		response.getOutputStream().write(bytes);

	}

	private String getBaseCmdUrl() {
		String baseUrl = "http://localhost:" + ControllerUrlLoader.DEFAULT_PORT + "/cmd";
		return baseUrl;
	}
}
