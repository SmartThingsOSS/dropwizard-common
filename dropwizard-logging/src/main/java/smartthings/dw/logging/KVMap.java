package smartthings.dw.logging;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class to more easily create maps for the KVLogger in a concise way. Similar to the some
 * of the guava helpers but allows null keys and parameter list can grow as large as needed.
 */
public class KVMap {

	private KVMap() {/* static only */}

	/**
	 * Create a unmodifiable map for passing into the KVLogger.
	 * @param entries An even number of entries where the even values are non-null and toString will be called.
	 * @return An unmodifiable map of the passed entries.
	 */
	static public Map<String, Object> of(Object... entries) {
		Map<String, Object> m = new LinkedHashMap<>();
		if (entries.length % 2 != 0) {
			throw new IllegalArgumentException("KVMap must have event number of values saw " + entries.length);
		}
		for (int i=0; i < entries.length; i=i+2) {
			m.put(entries[i].toString(), entries[i+1]);
		}
		return Collections.unmodifiableMap(m);
	}
}
