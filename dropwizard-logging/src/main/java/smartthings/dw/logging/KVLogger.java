package smartthings.dw.logging;

import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;

public class KVLogger {
	private KVLogger() {}

	public static void debug(Logger l, String key, Map<String, ? extends Object> params) {
		l.debug(buildLogMessage(key, params));
	}

	public static void debug(Logger l, String key) {
		debug(l, key, Collections.emptyMap());
	}

	public static void debug(Logger l, String key, Map<String, ? extends Object> params, Throwable t) {
		l.debug(buildLogMessage(key, params), t);
	}

	public static void info(Logger l, String key, Map<String, ? extends Object> params) {
		l.info(buildLogMessage(key, params));
	}

	public static void info(Logger l, String key) {
		info(l, key, Collections.emptyMap());
	}

	public static void info(Logger l, String key, Map<String, ? extends Object> params, Throwable t) {
		l.info(buildLogMessage(key, params), t);
	}

	public static void warn(Logger l, String key, Map<String, ? extends Object> params) {
		l.warn(buildLogMessage(key, params));
	}

	public static void warn(Logger l, String key) {
		warn(l, key, Collections.emptyMap());
	}

	public static void warn(Logger l, String key, Map<String, ? extends Object> params, Throwable t) {
		l.warn(buildLogMessage(key, params), t);
	}

	public static void error(Logger l, String key, Map<String, ? extends Object> params) {
		l.error(buildLogMessage(key, params));
	}

	public static void error(Logger l, String key) {
		error(l, key, Collections.emptyMap());
	}

	public static void error(Logger l, String key, Map<String, ? extends Object> params, Throwable t) {
		l.error(buildLogMessage(key, params), t);
	}

	public static void trace(Logger l, String key, Map<String, ? extends Object> params) {
		l.trace(buildLogMessage(key, params));
	}

	public static void trace(Logger l, String key) {
		trace(l, key, Collections.emptyMap());
	}

	public static void trace(Logger l, String key, Map<String, ? extends Object> params, Throwable t) {
		l.trace(buildLogMessage(key, params), t);
	}

	private static String buildLogMessage(String key, Map<String, ? extends Object> params) {
		StringBuilder sb = new StringBuilder(",key=").append(key);
		for ( Map.Entry<String, ? extends Object> e : params.entrySet()) {
			sb.append(", ").append(clean(e.getKey())).append('=').append(clean(e.getValue()));
		}
		sb.append(',');
		return sb.toString();
	}

	private static String clean(Object s) {
		if (s != null) {
			return s.toString().replace(",", "");
		}
		return null;
	}
}
