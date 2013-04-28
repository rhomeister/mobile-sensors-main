/**
 * 
 */
package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.io.Serializable;
import java.util.Arrays;

public class CacheEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Object[] values;

	public CacheEntry(Object... values) {
		this.values = values;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CacheEntry) {
			CacheEntry entry = (CacheEntry) obj;
			return Arrays.equals(values, entry.values);
		}

		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (Object value : values) {
			builder.append(value + " ");
		}

		return builder.toString();
	}
}