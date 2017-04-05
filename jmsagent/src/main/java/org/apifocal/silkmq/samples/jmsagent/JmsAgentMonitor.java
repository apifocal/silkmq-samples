/*
 * Copyright 2017 apifocal LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apifocal.silkmq.samples.jmsagent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A monitor that signals
 */
public class JmsAgentMonitor implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(JmsAgentMonitor.class);

    private Duration disconnectedThreshold;

    private final Map<String, Instant> agentCheckins = new HashMap<>();
    private String myId;

    public JmsAgentMonitor(String agentId, Duration disconnectedThreshold) {
        this.myId = agentId;
        this.disconnectedThreshold = disconnectedThreshold;
    }

    @Override
    public void onMessage(Message message) {
        try {
            printReceivedMessage(message);
            String remoteAgentId = message.getStringProperty(JmsAgentApp.AGENT_ID);
            if (myId.equals(remoteAgentId)) {
                throw new IllegalStateException("JMS filter error: Got a message from myself");
            }
            Instant lastSeen = agentCheckins.get(remoteAgentId);
            //Instant messageTimestamp = Instant.ofEpochMilli(message.getJMSTimestamp());
            Instant now = Instant.now();
            if (lastSeen == null) {
                LOG.info("Nice to meet you, agent {}", remoteAgentId);
            } else {
                Duration duration = Duration.between(lastSeen, now);
                if (duration.compareTo(disconnectedThreshold) > 0) {
                    LOG.info("Hello again, agent {}. You were away for {} seconds", remoteAgentId, duration.getSeconds());
                }
            }
            agentCheckins.put(remoteAgentId, now);

        } catch (Exception ex) {
            LOG.error("Receive error", ex);
            JmsAgentApp.interrupt();
        }
    }

    public static void printReceivedMessage(Message message) throws JMSException, IOException {
//        System.out.printf("Received: %s %s %s\n", message.getJMSMessageID(), JmsMessageUtil.getType(message), JmsMessageUtil.getContents(message));
        LOG.info("Received: {} (sent at {})", JmsMessageUtil.getContents(message), JmsMessageUtil.getTime(message));
    }
}
