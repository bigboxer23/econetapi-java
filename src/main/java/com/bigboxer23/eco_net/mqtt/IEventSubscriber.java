package com.bigboxer23.eco_net.mqtt;

import com.bigboxer23.eco_net.data.EcoNetMQTTEvent;
import java.io.IOException;

/** Interface to receive messages from EcoNet device via mqtt message */
public interface IEventSubscriber {

	void messageReceived(EcoNetMQTTEvent message) throws IOException;
}
