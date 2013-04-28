package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import uk.ac.soton.ecs.mobilesensors.Simulation;

public final class SimulationConfigurationReader {

	private SimulationConfigurationReader() {

	}

	private static Log log = LogFactory
			.getLog(SimulationConfigurationReader.class);

	public static Simulation read(File file) throws ConfigurationException {
		log.info("Reading simulation configuration from " + file);

		ApplicationContext context;
		if (file.isAbsolute()) {
			context = new ClassPathXmlApplicationContext(file.getPath());
		} else {
			context = new FileSystemXmlApplicationContext(file.getPath());
		}

		return (Simulation) context.getBean("simulation");
	}
}
