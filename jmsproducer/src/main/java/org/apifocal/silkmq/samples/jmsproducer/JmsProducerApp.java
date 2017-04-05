package org.apifocal.silkmq.samples.jmsproducer;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import sun.misc.Signal;

/**
 * JMS producer sample.
 */
public class JmsProducerApp {

    private static final boolean USE_BATCH = false;

    private static final AtomicBoolean interrupted = new AtomicBoolean(false);
    private static int counter = 1;

    public static void main(String[] args) {
        try {
            if (args.length != 6) {
                System.err.println("usage: java -jar jmsproducer.jar source jms-username jms-password jms-broker-url jms-destination");
                System.exit(-1);
            }

            String source = args[0];
            String username = args[1];
            String password = args[2];
            String brokerUrl = args[3];
            String destination = args[4];
            long delay = Long.parseLong(args[5]);

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection(username, password);
            connection.start();
            Session session = connection.createSession(USE_BATCH, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(destination);
            MessageProducer producer = session.createProducer(queue);

            // exit on Ctrl+C
            Signal.handle(new Signal("INT"), (Signal signal) -> {
                interrupted.set(true);
                System.err.printf("Exiting on SIG%s\n", signal.getName());
            });
            System.err.println("Press Ctrl+C to exit");

            // produce messages
            while (!interrupted.get()) {
                String text = createNewMessage(source);
                Message msg = session.createTextMessage(text);
                outputMessage(msg);
                producer.send(queue, msg);
                Thread.sleep(delay);
            }

            // this should be inside a finally clause, but for this sample the process terminates anyway
            connection.close();
        } catch (Exception ex) {
            System.err.println("Exception:");
            ex.printStackTrace();
        }

    }

    public static String createNewMessage(String source) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello #");
        builder.append(counter++);
        builder.append(" from ");
        builder.append(source);
        builder.append(" on ");
        builder.append(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        return builder.toString();
    }

    public static void outputMessage(Message message) throws JMSException, IOException {
        System.out.printf("Sending: %s\n", getJMSMessageContents(message));
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
