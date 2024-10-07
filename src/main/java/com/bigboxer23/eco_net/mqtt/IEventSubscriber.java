package com.bigboxer23.eco_net.mqtt;

import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/** Interface to receive messages from EcoNet device via mqtt message */
public interface IEventSubscriber {

	void messageReceived(String topic, MqttMessage message) throws IOException;
}
