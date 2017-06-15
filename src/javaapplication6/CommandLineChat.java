/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication6;


import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;

/**
 * @Biberacher Andreas
 */

public class CommandLineChat implements MessageListener{
    
    private String username;
    private Queue queueRequest;
    private Queue queueResponse;    
    private Context initialContext;
    private JMSContext jmsContext;
    private JMSProducer jmsProducer;
    

    public Queue getRequestQueue(){
        return this.queueRequest;
    }
    public Queue getResponseQueue(){
        return this.queueResponse;
    }
    

    public CommandLineChat createClient(String username, String requestQueue, String responseQueue) {
            
            this.username = username;

        try {
            this.initialContext = this.getInitalContext();
            this.queueRequest  = (Queue) initialContext.lookup(requestQueue);
            this.queueResponse = (Queue) initialContext.lookup(responseQueue);
            this.jmsContext = ((ConnectionFactory) this.initialContext.lookup("java:comp/DefaultJMSConnectionFactory")).createContext();
            
            //for incoming messages onMessage()is invoced
            jmsContext.createConsumer(queueRequest).setMessageListener(this);
            this.jmsProducer = jmsContext.createProducer();
        } catch (JMSException | NamingException ex) {
            Logger.getLogger(CommandLineChat.class.getName()).log(Level.SEVERE, null, ex);
        }
            System.out.println("CLIENT CREATED: username: " + username + " RequestQueue: " + requestQueue + " ResponseQueue: " + responseQueue);
            return this;
    }
    
    
    
    @Override
    public void onMessage(Message message) {
       
        try {
            System.out.println(message.getBody(String.class));
        } catch (JMSException ex) {
            Logger.getLogger(CommandLineChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public Context getInitalContext() throws JMSException, NamingException {
       //using corba factory
       Properties  properties = new Properties();
       properties.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
       properties.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
       //properties.setProperty("java.naming.provider.url", "iiop://localhost:3700");
       properties.setProperty("java.naming.provider.url", "iiop://ec2-35-162-176-107.us-west-2.compute.amazonaws.com:3700");
       return new InitialContext(properties);      
    }
   
    
            //Reading messages by SystemIn
            //ToDo: GetInputstream from GUI

    
    
    public void sendMessage(String message){
        if(message.equalsIgnoreCase("exit")){
                    jmsContext.close();
                }else{
                    jmsProducer.send(queueResponse, "["+username+": "+message+"]");
                }
    }
    

    
}

