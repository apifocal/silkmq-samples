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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 *
 */
public class JmsMessageUtil {

    public static String getType(Message m) {
        return m.getClass().getSimpleName();
    }

    protected static String getContents(Message message) throws JMSException {
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

}
