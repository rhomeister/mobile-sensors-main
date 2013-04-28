package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;

import org.junit.Ignore;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.GeneralGrid;
import uk.ac.soton.ecs.utils.test.ExtendedTestCase;

@Ignore
public class TestSimulationConfigurationReader extends ExtendedTestCase {
	private Simulation simulation;

	@Override
	protected void setUp() throws Exception {
		simulation = SimulationConfigurationReader.read(new File(
				"src/test/resources/main.xml"));
	}

	public void testUnixName() throws Exception {
		assertEquals("test", simulation.getSimpleName());
	}

	public void testDescriptionName() throws Exception {
		assertEquals("test description", simulation.getDescription());
	}

	public void testGrid() throws Exception {
		assertTrue(simulation.getEnvironment().getGrid() instanceof GeneralGrid);

		GeneralGrid grid = (GeneralGrid) simulation.getEnvironment().getGrid();

		assertEquals(grid.getGridPointCount(), 121);
	}

	public void testAccessibilityGraph() throws Exception {
		assertEquals(3, simulation.getEnvironment().getAccessibilityGraph()
				.getLocationCount());
	}
}
