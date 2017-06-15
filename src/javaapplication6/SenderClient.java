/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.jms.Queue;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.JMSException;
import javax.jms.QueueReceiver;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Properties;

public final class SenderClient implements MessageListener
{
   private final String username;
   private final String responseQueue;
   private final String replyQueue;
   
   private QueueSender sender;
   private Queue queueForReply; 
   private Queue queueForSending; 
   private QueueReceiver receiver;
   private QueueSession queueSession;
   private QueueConnection queueConnection;

   public SenderClient(String username, String replyQueue, String responseQueue) throws JMSException, NamingException
   {
      this.username=username;
      this.replyQueue = replyQueue;
      this.responseQueue = responseQueue;
      
       System.out.println("username: " + username + " replyQueue: " + replyQueue + " responseQueue: " + responseQueue);
      
       
      setupPTP();  // lookup context, set PTP connection and session
      setupSender();
      setupReceiver();
   }

   private void setupPTP() throws JMSException, NamingException
   {
      // step 1 create initialcontext
      System.out.println("about to create initialcontext");
      Properties env = new Properties();
      //env.put("org.omg.CORBA.ORBInitialHost","localhost");  // default ist localhost !!
      env.put("org.omg.CORBA.ORBInitialHost","ec2-35-162-176-107.us-west-2.compute.amazonaws.com"); 
      env.put("org.omg.CORBA.ORBInitialPort","3700");  // ist default
      env.put("java.naming.factory.initial","com.sun.enterprise.naming.SerialInitContextFactory");
      InitialContext ctx = new InitialContext(env);   // NamingException
      System.out.println("initialcontext received\n");

      // step 2 lookup connection factory
      System.out.println("try to lookup QueueConnectionFactory");
      QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory)ctx.lookup("jms/__defaultConnectionFactory");
      System.out.println("QueueConnectionFactory received\n");


      // step 3 use connection factory to create a JMS connection
      System.out.println("try to create a QueueConnection");
      queueConnection = queueConnectionFactory.createQueueConnection();
      System.out.println("JMS QueueConnection created\n");

      // step 4 use connection to create a session
      System.out.println("try to create a QueueSession");
      queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.println("QueueSession created\n");

      // step 5 lookup the Queue for sending
      System.out.println("try to lookup the Queue for sending");
      queueForSending = (Queue) ctx.lookup(responseQueue);
      System.out.println("Queue received\n");

      // step 5 lookup the Queue for reply
      System.out.println("try to lookup the Queue for the reply");
      queueForReply = (Queue) ctx.lookup(replyQueue);
      System.out.println("Queue received\n");

      queueConnection.start();
   }

   private void setupSender() throws JMSException
   {
      System.out.println("create sender");
      sender = queueSession.createSender(queueForSending);
   }

   private void setupReceiver() throws JMSException
   {
      System.out.println("create receiver");
      receiver = queueSession.createReceiver(queueForReply);
      System.out.println("set MessageListener for reply\n");
      receiver.setMessageListener(this);
      System.out.println("waiting for messages\n");
   }

   public void sendMessage(String text2Send) throws JMSException
   {
      TextMessage tm = queueSession.createTextMessage(text2Send);
      tm.setJMSReplyTo(queueForReply);
      sender.send(tm);
      //System.out.println("gesendet nach "+ queueForReply + ": " + tm.getText() + "\n");
   }

   // not in use
   public void stop() throws JMSException
   {
      queueConnection.stop();
      queueSession.close();
      queueConnection.close();
   }

   @Override
   public void onMessage(Message msg)
   {
      TextMessage tm = (TextMessage) msg;
      try
      {
         String mess = tm.getText();
         System.out.println("[" + username + "] recived: " + mess + "\n");
      }
      catch(JMSException ex)
      {
         ex.printStackTrace();
      }
   }

   public static void main(String args[]) throws Exception
   {
      System.out.println("starting the sender at " + new java.util.Date(System.currentTimeMillis()) + "\n");
      
      
      //"user01", "jms/Queue01", "jms/Queue02"
      //"user02", "jms/Queue02", "jms/Queue01"
      SenderClient client = new SenderClient(args[0], args[1], args[2]);      

           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
           String messageToSend = null;
           System.out.println("enter message...");
           while(true){

                messageToSend = bufferedReader.readLine();
                if(messageToSend.equalsIgnoreCase("exit")){
                    client.stop();
                    System.exit(0);
                }
                client.sendMessage(messageToSend);                
            }  
      }
      

      
      


//      for(int i=0; i<5; i++)
//      {
//         client1.sendMessage("" + new java.util.Date(System.currentTimeMillis()) );
//         Thread.sleep(1000);
//      }
//      
//            for(int i=0; i<5; i++)
//      {
//         client2.sendMessage("" + new java.util.Date(System.currentTimeMillis()) );
//         Thread.sleep(1000);
//      }
//            
//         client1.stop();
//         client2.stop();
      
}
