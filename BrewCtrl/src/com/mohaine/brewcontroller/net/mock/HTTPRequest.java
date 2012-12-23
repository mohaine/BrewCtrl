package com.mohaine.brewcontroller.net.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.mohaine.brewcontroller.util.StreamUtils;

public class HTTPRequest {

	private String queryString;
	private String postString;

	private String method = null;
	private String path = null;
	private String protocol = null;
	private InputStream inputStream;
	private Map<String, String> headerMap = new HashMap<String, String>();
	private Map<String, List<String>> paramMap = null;
	private Map<String, String> cookieMap;

	/**
	 * @param socket
	 * @throws IOException
	 */
	public HTTPRequest(Socket socket) throws IOException {
		super();
		inputStream = socket.getInputStream();
		socket.getOutputStream();
	}

	public void close() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public void readHeaders() throws IOException {

		int lineCount = 0;

		boolean foundEndOfHeader = false;
		while (true) {
			String line = StreamUtils.readLine(inputStream);
			if (line == null) {
				break;
			} else if (line.length() == 0) {
				foundEndOfHeader = true;
				break;
			}

			if (lineCount == 0) {
				int start = line.indexOf(" ");
				if (start > 0) {
					method = line.substring(0, start);
					start++;
					int end = line.indexOf(" ", start);
					if (end > 0) {

						int qIndex = line.indexOf('?', start);
						if (qIndex > -1) {
							queryString = line.substring(qIndex + 1, end);
							end = qIndex;
						}
						path = line.substring(start, end);
						protocol = line.substring(end);
					}
				}
			} else {
				int indexOf = line.indexOf(": ");
				if (indexOf > -1) {
					String headerName = line.substring(0, indexOf);
					String value = line.substring(indexOf + 2);
					headerMap.put(headerName, value);
				}
			}
			lineCount++;
		}

		if (foundEndOfHeader) {
			int contentLenght = getContentLength();
			if (contentLenght > 0) {
				byte[] buffer = new byte[contentLenght];

				int offset = 0;
				while (offset != contentLenght) {
					int size = inputStream.read(buffer, offset, buffer.length - offset);
					if (size < 0) {
						break;
					}
					offset += size;
				}

				if (offset == contentLenght) {
					String params = new String(buffer);
					postString = params;
				}
			}
		}
	}

	private void processParams(String params) {
		if (params != null && params.length() > 0) {
			StringTokenizer st = new StringTokenizer(params, "&");
			while (st.hasMoreTokens()) {
				String nameValue = st.nextToken();
				int eqIndex = nameValue.indexOf('=');
				if (eqIndex > 0) {
					String name = nameValue.substring(0, eqIndex);
					String value = nameValue.substring(eqIndex + 1);
					if (name.length() > 0) {
						name = urlDecode(name);
					}
					if (value.length() > 0) {
						value = urlDecode(value);
					}
					addParameter(name, value);
				}
			}
		}
	}

	private String urlDecode(String value) {
		try {
			return URLDecoder.decode(value, "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getCookieValue(String cname) {
		initCookies();
		return this.cookieMap.get(cname);
	}

	private void initCookies() {
		if (this.cookieMap == null) {
			this.cookieMap = new HashMap<String, String>();
			String cookiesString = getHeader("Cookie");
			if (cookiesString != null) {
				StringTokenizer st = new StringTokenizer(cookiesString, ";");
				while (st.hasMoreTokens()) {
					String cookieString = st.nextToken().trim();
					int eqIndex = cookieString.indexOf('=');
					if (eqIndex > -1) {
						String name = cookieString.substring(0, eqIndex);
						String value = cookieString.substring(eqIndex + 1);

						if (name.length() > 0) {
							name = urlDecode(name);
						}
						if (value.length() > 0) {
							value = urlDecode(value);
						}
						cookieMap.put(name, value);
					}
				}
			}
		}
	}

	private void addParameter(String name, String value) {
		List<String> paramList = paramMap.get(name);
		if (paramList == null) {
			paramList = new ArrayList<String>();
			paramMap.put(name, paramList);
		}
		paramList.add(value);
	}

	private int getContentLength() {
		int contentLenght = -1;
		String cl = getHeader("Content-Length");
		if (cl != null) {
			cl = cl.trim();
			if (cl.length() > 0) {
				contentLenght = Integer.parseInt(cl, 10);
			}
		}
		return contentLenght;
	}

	public String getHeader(String name) {
		return headerMap.get(name);
	}

	public String getParameter(String name) {
		initParams();
		List<String> paramList = paramMap.get(name);
		if (paramList != null && paramList.size() > 0) {
			return (String) paramList.get(0);
		}
		return null;
	}

	private void initParams() {
		if (paramMap == null) {
			paramMap = new HashMap<String, List<String>>();
			processParams(queryString);
			processParams(postString);
		}
	}

	public List<String> getParameterList(String name) {
		initParams();
		List<String> paramList = paramMap.get(name);
		if (paramList != null) {
			return Collections.unmodifiableList(paramList);
		}
		return null;
	}

	public String getPath() {
		return path;
	}

	public String getMethod() {
		return method;
	}

	public String getProtocol() {
		return protocol;
	}

	public String toString() {
		return method + " " + path + " " + protocol;
	}

}