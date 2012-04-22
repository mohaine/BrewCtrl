package com.mohaine.brewcontroller.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonPrettyPrint {

	private String newline = "\r\n";
	private String indent = "   ";
	private boolean stripNullAttributes;
	private int depth = 0;

	// public static void main(String[] args) throws IOException {
	// File dir = new File("../QActionBase/testdata");
	//
	// File[] files = dir.listFiles(new FileFilter() {
	//
	// @Override
	// public boolean accept(File arg0) {
	// return arg0.isFile() &&
	// arg0.getName().toLowerCase().endsWith("oryx.json");
	// }
	// });
	//
	// for (File file : files) {
	// if (file.exists()) {
	// String json = FileUtils.readFromFile(file);
	// String prettyPrint = new JsonPrettyPrint().prettyPrint(json);
	// FileUtils.writeToFile(prettyPrint, file);
	// } else {
	// System.out.println("Couln't find " + file.getAbsolutePath());
	// }
	// }
	//
	// }

	public String prettyPrint(String json) {
		depth = 0;
		StringBuffer sb = new StringBuffer();
		JsonParser jp = new OrderJsonParser(json);
		Object jsonObj = jp.parseJson();
		appendObject(sb, jsonObj);
		return sb.toString();
	}

	private void appendList(StringBuffer sb, List<Object> values) {
		startLevel(sb, "[");

		// if (depth == 2) {
		// Collections.sort(values, new Comparator<Object>() {
		//
		// @Override
		// public int compare(Object o1, Object o2) {
		//
		// Map o1Map = (Map) o1;
		// Map o2Map = (Map) o2;
		// return String.CASE_INSENSITIVE_ORDER.compare((String)
		// o1Map.get("name"), (String) o2Map.get("name"));
		// }
		// });
		// }

		int count = 0;
		if (values != null) {

			for (Object value : values) {
				int lengthBefore = sb.length();
				if (count > 0) {
					sb.append(',');
					sb.append(newline);
				}
				printDepth(sb);
				boolean appendObject = appendObject(sb, value);
				if (appendObject) {
					count++;
				} else {
					sb.setLength(lengthBefore);
				}
			}
		}

		endLevel(sb, "]");
	}

	private void printDepth(StringBuffer sb) {
		for (int i = 0; i < depth; i++) {
			sb.append(indent);
		}
	}

	private void appendMap(StringBuffer sb, Map<String, Object> map) {
		startLevel(sb, "{");

		int count = 0;

		OrderedMap<String, Object> om = (OrderedMap<String, Object>) map;

		for (String key : om.keysInOrder) {
			Object value = map.get(key);

			if (value != null || !stripNullAttributes) {
				if (count > 0) {
					sb.append(',');
					sb.append(newline);
				}
				printDepth(sb);
				appendString(sb, key);
				sb.append(':');
				if (!appendObject(sb, value)) {
					sb.append("null");
				}
				count++;
			}
		}
		endLevel(sb, "}");

	}

	private void endLevel(StringBuffer sb, String charC) {
		boolean addNewLine = false;
		if (sb.length() > newline.length()) {
			for (int i = 1; i <= newline.length(); i++) {
				char newLineChar = newline.charAt(newline.length() - i);
				char sbChar = sb.charAt(sb.length() - i);
				if (newLineChar != sbChar) {
					addNewLine = true;
					break;
				}
			}
		}
		if (addNewLine) {
			sb.append(newline);
		}
		depth--;
		printDepth(sb);
		sb.append(charC);
	}

	private void startLevel(StringBuffer sb, String charV) {
		sb.append(charV);
		sb.append(newline);
		depth++;
	}

	@SuppressWarnings("unchecked")
	private boolean appendObject(StringBuffer sb, Object value) {
		if (value == null) {
			sb.append("null");
		} else {
			if (value instanceof String) {
				appendString(sb, (String) value);
			} else if (value instanceof Number) {
				sb.append(value.toString());
			} else if (value instanceof Boolean) {
				appendBoolean(sb, value);
			} else if (value instanceof List) {
				appendList(sb, (List<Object>) value);
			} else if (value instanceof Map) {
				appendMap(sb, (Map<String, Object>) value);
			}
		}
		return true;
	}

	private void appendBoolean(StringBuffer sb, Object value) {
		if (((Boolean) value).booleanValue()) {
			sb.append("true");
		} else {
			sb.append("false");
		}
	}

	private void appendString(StringBuffer sb, String name) {
		if (name == null) {
			sb.append("null");
		} else {
			sb.append('"');
			escapeStringForJson(sb, name);
			sb.append("\"");
		}
	}

	private void escapeStringForJson(StringBuffer sb, String value) {

		if (value == null) {
			sb.append("null");
		} else {
			char[] toCharArray = value.toCharArray();
			for (int i = 0; i < toCharArray.length; i++) {
				char curChar = toCharArray[i];

				if (curChar == '"') {
					sb.append('\\');
					sb.append(curChar);
				} else if (curChar == '\'') {
					sb.append('\\');
					sb.append(curChar);
				} else if (curChar == '\\') {
					sb.append('\\');
					sb.append(curChar);
				} else if (curChar == '\b') {
					sb.append("\\b");
				} else if (curChar == '\f') {
					sb.append("\\f");
				} else if (curChar == '\n') {
					sb.append("\\n");
				} else if (curChar == '\r') {
					sb.append("\\r");
				} else if (curChar == '\t') {
					sb.append("\\t");
				} else if (curChar > 255) {
					sb.append("\\u");
					String hex = Integer.toHexString(curChar);
					for (int offset = hex.length(); offset < 4; offset++) {
						sb.append('0');
					}
					sb.append(hex);
				} else {
					sb.append(curChar);
				}

			}
		}
	}

	public void setNewLine(String newline) {
		this.newline = newline;

	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	private static final class OrderedMap<K, V> implements Map<K, V> {
		private Map<K, V> map;
		private ArrayList<K> keysInOrder = new ArrayList<K>();

		public OrderedMap(Map<K, V> map) {
			super();
			this.map = map;
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		@Override
		public V get(Object key) {
			return map.get(key);
		}

		@Override
		public V put(K key, V value) {
			keysInOrder.remove(key);
			keysInOrder.add(key);
			return map.put(key, value);
		}

		@Override
		public V remove(Object key) {
			keysInOrder.remove(key);
			return map.remove(key);
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> m) {
			Set<? extends K> keySet = m.keySet();
			for (K k : keySet) {
				keysInOrder.remove(k);
				keysInOrder.add(k);
			}
			map.putAll(m);
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public Set<K> keySet() {
			return map.keySet();
		}

		@Override
		public Collection<V> values() {
			return map.values();
		}

		@Override
		public Set<java.util.Map.Entry<K, V>> entrySet() {
			return map.entrySet();
		}

	}

	private static final class OrderJsonParser extends JsonParser {
		private OrderJsonParser(String jsonString) {
			super(jsonString);
		}

		@Override
		protected Map<String, Object> createMap() {
			return new OrderedMap<String, Object>(super.createMap());
		}
	}

	public boolean isStripNullAttributes() {
		return stripNullAttributes;
	}

	public void setStripNullAttributes(boolean stripNullValues) {
		this.stripNullAttributes = stripNullValues;
	}

}
