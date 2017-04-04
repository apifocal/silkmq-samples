package org.apifocal.silkmq.samples.jmsconsumer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import sun.misc.Signal;

/**
 * JMS consumer sample.
 */
public class JmsConsumerApp {

    protected static final String BROKER_URL = "tcp://sg01.silkmq.org";
    protected static final boolean USE_BATCH = false;

    private static AtomicBoolean interrupted = new AtomicBoolean(false);

    public static void main(String[] args) {
        try {
            if (args.length != 4) {
                System.err.println("usage: java -jar blah.jar jms-username jms-password jms-broker-url jms-destination");
                System.exit(-1);
            }

            String username = args[0];
            String password = args[1];
            String brokerUrl = args[2];
            String destination = args[3];

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection(username, password);
            connection.start();
            Session session = connection.createSession(USE_BATCH, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(destination);
            MessageConsumer consumer = session.createConsumer(queue);

            // exit on Ctrl+C
            Signal.handle(new Signal("INT"), (Signal signal) -> {
                interrupted.set(true);
                System.err.printf("Exiting on SIG%s\n", signal.getName());
            });
            System.err.println("Press Ctrl+C to exit");

            // receive and dump messages
            while (!interrupted.get()) {
                Message msg = consumer.receive(100);
                if (msg != null) {
                    outputMessage(msg);
                }
            }

            // this should be inside a finally clause, but for this sample the process terminates anyway
            connection.close();
        } catch (Exception ex) {
            System.err.println("Exception:");
            ex.printStackTrace();
        }

    }

    public static void outputMessage(Message message) throws JMSException, IOException {
        System.out.printf("Received: %s %s %s\n", message.getJMSMessageID(), getJMSType(message), getJMSMessageContents(message));
    }

    protected static String getJMSMessageContents(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            return ((TextMessage) message).getText();
        } else if (message instanceof BytesMessage) {
            StringBuilder builder = new StringBuilder();

            BytesMessage bm = (BytesMessage) message;
            long l = bm.getBodyLength();
            byte[] bb = new byte[(int) l];
            bm.readBytes(bb, (int) l);
            for (int i = 0; i < l; i++) {
                byte b = bb[i];
                builder.append(String.format("%02X ", b));
                if (((i + 1) % 16) == 0) {
                    builder.append("\n");
                }
            }
            builder.append("\n");
            return builder.toString();
        } else {
            return "[Not a text message]";
        }
    }

    public static String getJMSType(Message m) {
        return m.getClass().getSimpleName();
    }
}
