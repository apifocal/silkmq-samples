# SilkMQ samples

## JMS consumer

    java -jar jmsproducer/target/jmsproducer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
                   Alice karaf karaf tcp://localhost:61616 queue://Test.Queue 1000
                     |      |     |          |                       |           |
         name -------+      |     |          |                       |           |
         username ----------+     |          |                       |           |
         password ----------------+          |                       |           |
         broker URL -------------------------+                       |           |
         queue ------------------------------------------------------+           |
         message delay in millis ------------------------------------------------+

## JMS producer

    java -jar jmsconsumer/target/jmsconsumer-1.0.0-SNAPSHOT-jar-with-dependencies.jar \
                   karaf karaf tcp://localhost:61616 queue://Test.Queue
                     |     |          |                       |
        username ----+     |          |                       |
        password ----------+          |                       |
        broker URL -------------------+                       |
        queue ------------------------------------------------+

