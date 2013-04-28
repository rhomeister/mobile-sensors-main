package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class Cache<V> implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Map<CacheEntry, V> cache = new FastMap<CacheEntry, V>();

	public void cacheValue(V value, Object... entry) {
		cache.put(new CacheEntry(entry), value);
	}

	public V getCachedValue(Object... entry) {
		return cache.get(new CacheEntry(entry));
	}

	public static Cache<List<Location>> loadFromFile(File cacheFile)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(
				cacheFile));

		Cache<List<Location>> cache = (Cache<List<Location>>) stream
				.readObject();
		stream.close();
		return cache;
	}

	public void writeToFile(File cacheFile) throws FileNotFoundException,
			IOException {
		ObjectOutputStream stream = new ObjectOutputStream(
				new FileOutputStream(cacheFile));

		stream.writeObject(this);
		stream.flush();
		stream.close();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (CacheEntry entry : cache.keySet()) {
			builder.append(String.format("%40s %20s\n", entry, cache.get(entry)));
		}

		return builder.toString();
	}
}
