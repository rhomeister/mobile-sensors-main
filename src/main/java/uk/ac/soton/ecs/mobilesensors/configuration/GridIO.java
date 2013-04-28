package uk.ac.soton.ecs.mobilesensors.configuration;

import org.springframework.core.io.Resource;

import uk.ac.soton.ecs.mobilesensors.layout.GeneralGrid;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;

public class GridIO {
	private Resource resource;

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Grid create() throws Exception {
		return GeneralGrid.read(resource.getFile());
	}
}
