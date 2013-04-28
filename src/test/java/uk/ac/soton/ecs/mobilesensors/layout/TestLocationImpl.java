package uk.ac.soton.ecs.mobilesensors.layout;

import junit.framework.TestCase;

public class TestLocationImpl extends TestCase {
	private LocationImpl location1;
	private LocationImpl location2;
	private LocationImpl location3;

	@Override
	protected void setUp() throws Exception {
		location1 = new LocationImpl(10, 40);
		location2 = new LocationImpl(10, 40);
		location3 = new LocationImpl(40, 90);
	}

	public void testEquals() throws Exception {
		assertTrue(location1.equals(location1));
		assertTrue(location1.equals(location2));
		assertTrue(location2.equals(location1));
		assertFalse(location3.equals(location1));
		assertFalse(location2.equals(location3));
	}
}
