package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropertyValues {
	private Properties prop;
	private String propFileName;
	
	public GetPropertyValues(String propFileName) {
		this.propFileName = propFileName;
	}
	
	public boolean loadProperties() {
		prop = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		
		if(inputStream != null) {
			try {
				prop.load(inputStream);
			} catch (IOException e) {
				System.out.println("An exception occured while loading the properties file : " + e);
			}
			finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					System.out.println("An exception occured while closing the inputStream of properties file : " + e);
				}
			}
			
			return true;
		}
		else
			return false;
	}

	public Properties getProperties() {
		return prop;
	}
}
