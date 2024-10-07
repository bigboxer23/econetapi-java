package com.bigboxer23.eco_net;

/** */
public interface IEcoNetConstants {
	String MQTT_URL = "tcp://rheem.clearblade.com:1883";
	String baseUrl = "https://rheem.clearblade.com/api/v/1/";
	String CLEAR_BLADE_SYSTEM_KEY = "e2e699cb0bb0bbb88fc8858cb5a401";
	String CLEAR_BLADE_SYSTEM_SECRET = "E2E699CB0BE6C6FADDB1B0BC9A20";
	int TIMEOUT = 60000;
	int QOS = 2;
}
