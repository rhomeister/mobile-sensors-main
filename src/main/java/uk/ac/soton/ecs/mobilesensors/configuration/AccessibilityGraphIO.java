package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;

public class AccessibilityGraphIO {

	private Resource resource;
	private double scale = 1.0;

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getScale() {
		return scale;
	}

	public static AccessibilityGraphImpl readGraph(File file)
			throws IOException {
		return AccessibilityGraphImpl.readGraph(file);
	}

	public AccessibilityGraphImpl create() throws IOException {
		return AccessibilityGraphImpl.readGraph(resource.getFile(), scale);
	}

}
