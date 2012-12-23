package com.mohaine.brewcontroller.net;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class URLRequest implements Serializable {

	public static class Cookie {
		String name;
		String value;

		public Cookie() {

		}

		public Cookie(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public static class CookieContext {
		private final List<Cookie> cookiesX = new ArrayList<Cookie>();

		public void set(String name, String value) {
			synchronized (cookiesX) {
				for (Cookie cookie : cookiesX) {
					if (cookie.getName().equals(name)) {
						cookie.setValue(value);

						return;
					}
				}
				cookiesX.add(new Cookie(name, value));
			}
		}

		public int size() {
			synchronized (cookiesX) {
				return cookiesX.size();
			}
		}

		public List<Cookie> get() {
			synchronized (cookiesX) {
				List<Cookie> cookies = new ArrayList<Cookie>();
				cookies.addAll(cookiesX);
				return cookies;
			}
		}
	}

	private static final long serialVersionUID = 1L;

	// timeout in ms
	private final int timeout = 60 * 1000;

	private int read;
	private URL url = null;
	private final URL referer = null;

	// url connection vars
	private transient URLConnection openSocket;
	private transient String contentType;
	private transient int contentLength;
	private transient InputStream inputStream;
	//
	private List<String[]> postParameters = null;
	private List<String[]> requestHeaders = null;
	private byte[] postData;

	private Map<String, List<String>> headers;
	private CookieContext cookieContext;

	public URLRequest(URL url) {
		this.url = cleanUrl(url);
	}

	public URLRequest(URL url, CookieContext cookieContext) {
		this.url = cleanUrl(url);
		this.cookieContext = cookieContext;
	}

	{
		try {
			reset();
		} catch (Exception e) {
			// ignore
		}
	}

	public void reset() throws IOException {
		read = 0;
		contentType = null;
		contentLength = -1;
		headers = null;

		close();

	}

	@Override
	public boolean equals(Object o) {
		boolean equal = false;
		if (o instanceof URLRequest) {
			URLRequest other = (URLRequest) o;
			equal = true;
			if (url != other.url) {
				equal = url != null && url.equals(other.url);
			}
		}
		return equal;
	}

	// fixes up the url ..... the web standards suck
	private URL cleanUrl(URL url) {
		String file = url.getFile();
		String host = url.getHost();
		int port = url.getPort();
		String protocol = url.getProtocol();

		// remove any spaces in file portion
		StringBuffer buffer = new StringBuffer(file.length() + 20);

		for (int i = 0, size = file.length(); i < size; i++) {
			char currentChar = file.charAt(i);

			switch (currentChar) {
			case ' ':
				buffer.append('%');
				buffer.append('2');
				buffer.append('0');
				break;
			default:
				buffer.append(currentChar);
				break;
			}
		}
		file = buffer.toString();

		// recreate url using cleaned values
		try {
			url = new URL(protocol, host, port, file);
		} catch (Exception e) {
			// we made things worse, just use the old value
		}

		return url;
	}

	public Map<String, List<String>> getHeaders() throws IOException {
		startRequestIfNeeded();
		return headers;
	}

	public URL getUrl() {
		return url;
	}

	public URL getReferer() {
		return referer;
	}

	public String getContentType() {
		return contentType;
	}

	public int getContentLength() {
		return contentLength;
	}

	public int getReadSize() {
		return read;
	}

	public void addHeaders() throws IOException {

		if (referer != null) {
			openSocket.setRequestProperty("Referer", referer.toString());
		}

		if (requestHeaders != null) {
			for (String[] header : requestHeaders) {
				openSocket.setRequestProperty(header[0], header[1]);
			}
		}

		openSocket.setRequestProperty("User-Agent", "BrewCtrl");

		if (cookieContext != null && cookieContext.size() > 0) {
			StringBuffer cookiesString = new StringBuffer();

			for (Cookie cookie : cookieContext.get()) {
				if (cookiesString.length() > 0) {
					cookiesString.append("; ");
				}

				cookiesString.append(cookie.getName());
				cookiesString.append("=");
				cookiesString.append(cookie.getValue());
			}
			openSocket.setRequestProperty("Cookie", cookiesString.toString());
		}
	}

	public void startRequestIfNeeded() throws IOException {
		if (openSocket == null) {
			openSocket = url.openConnection();
			addHeaders();

			if (postData == null) {
				postData = generatePostData();
			}
			if (postData != null) {
				openSocket.setRequestProperty("Content-Length", Integer.toString(postData.length));
				openSocket.setDoOutput(true);
				OutputStream outputStream = openSocket.getOutputStream();
				outputStream.write(postData);
			}

			inputStream = openSocket.getInputStream();
			contentLength = openSocket.getContentLength();
			contentType = openSocket.getContentType();
			headers = openSocket.getHeaderFields();

			// reload URL incase of redirect
			url = openSocket.getURL();

		}
	}

	private byte[] generatePostData() throws IOException, UnsupportedEncodingException {

		if (postParameters != null) {
			int count = 0;
			StringBuffer sb = new StringBuffer();
			for (Iterator<String[]> iter = postParameters.iterator(); iter.hasNext();) {
				String[] param = iter.next();

				if (count > 0) {
					sb.append("&");
				}
				sb.append(urlEncode(param[0]));
				sb.append("=");
				sb.append(urlEncode(param[1]));
			}
			return sb.toString().getBytes();
		}
		return null;
	}

	private String urlEncode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF8");
	}

	public void save(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			save(fos);
			close();
		} catch (IOException e) {
			fos.close();
			file.delete();
			throw e;
		}
		fos.close();
	}

	public String getString() throws IOException {
		return new String(getByteArray());
	}

	public byte[] getByteArray() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		startRequestIfNeeded();
		save(os);
		close();
		return os.toByteArray();
	}

	public void close() {
		if (inputStream != null) {
			try {
				inputStream.close();
				inputStream = null;
			} catch (Exception e) {
				// ignore
			}
		}

		if (openSocket != null) {
			openSocket = null;
		}

	}

	public void save(OutputStream outputStream) throws IOException {
		startRequestIfNeeded();
		byte[] buffer = new byte[10000];
		Date lastTime = new Date();
		while (contentLength < 0 || contentLength > read) {
			Date time = new Date();
			if (time.getTime() > lastTime.getTime() + timeout) {
				throw new IOException("Connection timed out.");
			}
			int bytes = inputStream.read(buffer, 0, buffer.length);
			if (bytes > 0) {
				// we got some data so reset timmer.
				lastTime = time;

				outputStream.write(buffer, 0, bytes);
				read += bytes;

				// allow something else to do some work
				Thread.yield();
			} else if (bytes < 0) {
				// Done with file
				break;
			} else {
				// No data was ready, wait 200 ms for more to show up
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Ingore
				}
			}
		}

		String headerField = openSocket.getHeaderField("Set-Cookie");
		if (headerField != null) {

			int eqInd = headerField.indexOf('=');
			if (eqInd > 0) {
				int endIndex = headerField.indexOf(';', eqInd);
				if (endIndex < 0) {
					endIndex = headerField.length();
				}
				String name = headerField.substring(0, eqInd);
				String value = headerField.substring(eqInd + 1, endIndex);
				setCookie(name, value);
			}

		}

	}

	@Override
	public String toString() {
		return url.toString();
	}

	public void setCookie(String name, String value) {
		if (cookieContext == null) {
			cookieContext = new CookieContext();
		}
		cookieContext.set(name, value);
	}

	public void addParameter(String name, String value) {
		if (postParameters == null) {
			postParameters = new ArrayList<String[]>();
		}
		postParameters.add(new String[] { name, value });
	}

	public void setPostData(byte[] bytes) {
		postData = bytes;
	}

	public void addHeader(String name, String value) {
		if (requestHeaders == null) {
			requestHeaders = new ArrayList<String[]>();
		}
		requestHeaders.add(new String[] { name, value });
	}

	public CookieContext getCookieContext() {
		return cookieContext;
	}

	public void setCookieContext(CookieContext cookieContext) {
		this.cookieContext = cookieContext;
	}

	public String getHeader(String name) throws IOException {
		Map<String, List<String>> headers = getHeaders();
		List<String> headersWithName = headers.get(name);
		if (headersWithName != null && headersWithName.size() > 0) {
			return headersWithName.get(0);
		}
		return null;
	}

}
