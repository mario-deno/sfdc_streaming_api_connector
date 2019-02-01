package it.fastweb.edh.jms;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class JmsSink {



    // Defines the JMS context factory.
    public final static String JMS_FACTORY="jms/ConnFactory/ConnFactoryProc";


    private QueueConnectionFactory qconFactory;
    private QueueConnection qcon;
    private QueueSession qsession;
    private QueueSender qsender;
    private Queue queue;
    private TextMessage msg;

    /**
     * Creates all the necessary objects for sending
     * messages to a JMS queue.
     *
     * @param JNDI initial context
     * @param queueName name of queue
     * @exception NamingException if operation cannot be performed
     * @exception JMSException if JMS fails to initialize due to internal error
     */
    public void init(Context ctx, String queueName)
            throws NamingException, JMSException
    {
        qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) ctx.lookup(queueName);
        qsender = qsession.createSender(queue);
        msg = qsession.createTextMessage();
        qcon.start();
    }

    /**
     * Sends a message to a JMS queue.
     *
     * @param message  message to be sent
     * @exception JMSException if JMS fails to send message due to internal error
     */
    public void send(String message) throws JMSException {
        msg.setText(message);
        qsender.send(msg);
    }

    /**
     * Closes JMS objects.
     * @exception JMSException if JMS fails to close objects due to internal error
     */
    public void close() throws JMSException {
        qsender.close();
        qsession.close();
        qcon.close();
    }

    /** main() method.
     *


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java examples.jms.queue.QueueSend WebLogicURL");
            return;
        }
        InitialContext ic = getInitialContext(args[0]);
        // Defines the queue.

        String QUEUE="jms/Queue/;

        App qs = new App();

        qs.init(ic, QUEUE);
        System.out.println("init done");

        readAndSend(qs);

        qs.close();
    }
*/

}
