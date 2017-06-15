/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testUnits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javaapplication6.CommandLineChat;

public class TestUnit1 {
    
        
    public static void main (String[]args) throws IOException{
        CommandLineChat chat = new CommandLineChat().createClient("user1","jms/Queue01","jms/Queue02");
  
        
        //Reading messages by SystemIn
            //ToDo: GetInputstream from GUI
           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
           String messageToSend = null;
           while(true){
                messageToSend = bufferedReader.readLine();
                if(messageToSend.equalsIgnoreCase("exit")){
                    chat.sendMessage(messageToSend);
                    System.exit(0);
                }
                chat.sendMessage(messageToSend);
            }
    
    }
    
    
}
