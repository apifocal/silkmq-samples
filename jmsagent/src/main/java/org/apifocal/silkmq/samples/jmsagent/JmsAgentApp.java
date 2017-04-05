package org.apifocal.silkmq.samples.jmsagent;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;

/**
 * Sample producer-consumer JMS client.
 */
public class JmsAgentApp {

    private static final Logger LOG = LoggerFactory.getLogger(JmsAgentApp.class);

    public static final String AGENT_ID = "AgentId";
    private static final boolean USE_BATCH = false;

    private static AtomicBoolean interrupted = new AtomicBoolean(false);
    private static int counter = 1;

    public static void main(String[] args) {
        try {
            if (args.length != 5) {
                System.err.println("usage: java -jar jmsagent.jar agent-id jms-username jms-password jms-broker-url delay");
                System.exit(-1);
            }

            String agentId = args[0];
            String username = args[1];
            String password = args[2];
            String brokerUrl = args[3];
            long delay = Long.parseLong(args[4]);

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection(username, password);
            connection.start();
            Session session = connection.createSession(USE_BATCH, Session.AUTO_ACKNOWLEDGE);

            // start receiving messages
            Queue queue = session.createQueue("SilkMQ.Demo.JMSAgents");
            String selector = AGENT_ID + " <> '" + agentId + "'";
            MessageConsumer consumer = session.createConsumer(queue, selector);
            consumer.setMessageListener(new JmsAgentMonitor(agentId, Duration.ofMillis(delay * 5)));

            // exit on Ctrl+C
            Signal.handle(new Signal("INT"), (Signal signal) -> {
                interrupted.set(true);
                LOG.info("Exiting on SIG{}", signal.getName());
                try {
                    connection.close();
                } catch (JMSException ex) {
                    LOG.error("Exception", ex);
                }
            });
            LOG.info("Press Ctrl+C to exit");

            // and send some messages in the meantime
            MessageProducer producer = session.createProducer(queue);

            // generate some messages
            while (!interrupted.get()) {
                String text = createNewMessage(agentId);
                Message msg = session.createTextMessage(text);
                msg.setStringProperty(AGENT_ID, agentId);
                printSentMessage(msg);
                producer.send(queue, msg);
                Thread.sleep(delay);
            }

            // this should be inside a finally clause, but for this sample the process terminates anyway
            connection.close();
        } catch (Exception ex) {
            LOG.error("Exception", ex);
        }

    }

    public static void printSentMessage(Message message) throws JMSException, IOException {
        LOG.info("Sending : {}", JmsMessageUtil.getContents(message));
    }

    public static String createNewMessage(String source) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello #");
        builder.append(counter++);
        builder.append(" from ");
        builder.append(source);
        return builder.toString();
    }

    public static void interrupt() {
        interrupted.set(true);
    }
}
