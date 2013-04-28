package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;

import junit.framework.TestCase;
import uk.ac.soton.ecs.mobilesensors.layout.GeneralGrid;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.RectangularGrid;

public class TestGridIO extends TestCase {
	private RectangularGrid grid;
	private File file;

	@Override
	protected void setUp() throws Exception {
		grid = new RectangularGrid(10.0, 5.0, 11);
		file = File.createTempFile("temp", "");
	}

	public void testIO() throws Exception {
		grid.write(file);

		Grid readGrid = GeneralGrid.read(file);

		assertEquals(grid, readGrid);
	}

	@Override
	protected void tearDown() throws Exception {
		file.delete();
	}
}
