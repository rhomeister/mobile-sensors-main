package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public class SensorConfigurationReader {
	private static Log log = LogFactory.getLog(SensorConfigurationReader.class);

	public static Sensor load(File file) {
		log.info("Reading sensor configuration from " + file);

		ApplicationContext context = new FileSystemXmlApplicationContext(file
				.getPath());

		return (Sensor) context.getBean("sensor");
	}
}
