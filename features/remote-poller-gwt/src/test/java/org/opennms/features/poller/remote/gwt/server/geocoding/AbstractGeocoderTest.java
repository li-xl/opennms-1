package org.opennms.features.poller.remote.gwt.server.geocoding;

public class AbstractGeocoderTest {

	protected boolean shouldRun() {
		if (getApiKey() != null) {
			return true;
		} else {
			System.err.println("skipping test, no API key found");
			return false;
		}
	}

	protected String getApiKey() {
		return System.getProperty("gwt.apikey");
	}

}
