package org.opennms.features.poller.remote.gwt.server.geocoding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import net.simon04.jelementtree.ElementTree;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;

public class MapquestGeocoder implements Geocoder {

	public enum Quality {
		COUNTRY,
		STATE,
		ZIP,
		COUNTY,
		ZIP_EXTENDED,
		CITY,
		STREET,
		INTERSECTION,
		ADDRESS,
		POINT
	}

	private static final String GEOCODE_URL = "http://www.mapquestapi.com/geocoding/v1/address?callback=renderGeocode&outFormat=xml";
	private static final HttpClient m_httpClient = new HttpClient();
	private String m_apiKey;
	private Quality m_minimumQuality;
	private String m_referer;


	public MapquestGeocoder() {
		m_apiKey = System.getProperty("gwt.apikey");
		m_referer = System.getProperty("gwt.geocoder.referer");
		final String minimumQuality = System.getProperty("gwt.geocoder.minimumQuality");
		if (minimumQuality != null) {
			m_minimumQuality = Quality.valueOf(minimumQuality);
		}
	}

	public MapquestGeocoder(String apiKey) {
		this();
		m_apiKey = apiKey;
	}

	public GWTLatLng geocode(final String geolocation) throws GeocoderLookupException {
		final HttpMethod method = new GetMethod(getUrl(geolocation));
		method.addRequestHeader("User-Agent", "OpenNMS-MapQuestGeocoder/1.0");
		method.addRequestHeader("Referer", m_referer);

		try {
			m_httpClient.executeMethod(method);
			final ElementTree tree = ElementTree.fromStream(method.getResponseBodyAsStream());
			if (tree == null) {
				throw new GeocoderLookupException("an error occurred connecting to the MapQuest geocoding service (no XML tree was found)");
			}

			final ElementTree statusCode = tree.find("//statusCode");
			if (statusCode == null || !statusCode.getText().equals("0")) {
				final String code = (statusCode == null? "unknown" : statusCode.getText());
				final ElementTree messageTree = tree.find("//message");
				final String message = (messageTree == null? "unknown" : messageTree.getText());
				throw new GeocoderLookupException(
					"an error occurred when querying MapQuest (statusCode=" + code + ", message=" + message + ")"
				);
			}

			final List<ElementTree> locations = tree.findAll("//location");
			if (locations.size() > 1) {
				LogUtils.warnf(this, "more than one location returned for query: %s", geolocation);
			} else if (locations.size() == 0) {
				throw new GeocoderLookupException("MapQuest returned an OK status code, but no locations");
			}
			final ElementTree location = locations.get(0);

			// first, check the quality
			if (m_minimumQuality != null) {
				final Quality geocodeQuality = Quality.valueOf(location.find("//geocodeQuality").getText().toUpperCase());
				if (geocodeQuality.compareTo(m_minimumQuality) < 0) {
					throw new GeocoderLookupException("response did not meet minimum quality requirement (" + geocodeQuality + " is less specific than " + m_minimumQuality + ")");
				}
			}

			// then, extract the lat/lng
			final ElementTree latLng = location.find("//latLng");
			Double latitude = Double.valueOf(latLng.find("//lat").getText());
			Double longitude = Double.valueOf(latLng.find("//lng").getText());
			return new GWTLatLng(latitude, longitude);
		} catch (GeocoderLookupException e) {
			throw e;
		} catch (Exception e) {
			throw new GeocoderLookupException("unable to get lat/lng from MapQuest", e);
		}
	}

	private String getUrl(String geolocation) throws GeocoderLookupException {
		try {
			return GEOCODE_URL + "&key=" + m_apiKey + "&location=" + URLEncoder.encode(geolocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GeocoderLookupException("unable to URL-encode query string", e);
		}
	}

}
