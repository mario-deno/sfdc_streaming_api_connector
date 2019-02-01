package it.fastweb.edh.jms;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

public class WeblogicHelper

{
    // Defines the JNDI context factory.


    public static InitialContext getInitialContext(String initialContextFactory, String url)
            throws NamingException
    {
        //Applications use JNDI naming services to locate queue in JMS

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        env.put(Context.PROVIDER_URL, url);
        //env.put(Context.SECURITY_PRINCIPAL,"User");
        //env.put(Context.SECURITY_CREDENTIALS,"***");
        return new javax.naming.InitialContext(env);
    }

}
