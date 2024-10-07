package com.bigboxer23.eco_net.mqtt;

import com.bigboxer23.eco_net.IEcoNetConstants;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/** Connection options for connecting to the EcoNet MQTT server */
public class EcoNetMQTTConnectOptions extends MqttConnectOptions implements IEcoNetConstants {
	public EcoNetMQTTConnectOptions(String userToken) {
		setCleanSession(true);
		setAutomaticReconnect(true);
		setUserName(userToken);
		setPassword(CLEAR_BLADE_SYSTEM_KEY.toCharArray());
	}
}
