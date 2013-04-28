package uk.ac.soton.ecs.mobilesensors.layout;

import junit.framework.TestCase;

public class TestAccessibilityRelationImpl extends TestCase {
	private AccessibilityRelationImpl relation1;
	private AccessibilityRelationImpl relation2;
	private AccessibilityRelationImpl relation3;

	@Override
	protected void setUp() throws Exception {
		AccessibilityGraphImpl dummy = new AccessibilityGraphImpl();

		Location location1 = dummy.addAccessibleLocation(10, 30);
		Location location2 = dummy.addAccessibleLocation(10, 50);

		relation1 = new AccessibilityRelationImpl(location1, location1);
		relation2 = new AccessibilityRelationImpl(location1, location1);
		relation3 = new AccessibilityRelationImpl(location1, location2);
	}

	public void testEquals() throws Exception {
		assertEquals(relation1, relation1);
		assertEquals(relation1, relation2);
		assertEquals(relation2, relation1);
		assertEquals(relation2, relation2);
		assertEquals(relation3, relation3);

		assertFalse(relation1.equals(relation3));
		assertFalse(relation2.equals(relation3));
		assertFalse(relation3.equals(relation1));
	}
}
