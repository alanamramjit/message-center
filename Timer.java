import java.net.*;
import java.util.*;
import java.io.*;

public class Timer extends Thread{
       private final long TIMER = 60000;
       private HashMap<String, User> check;

       public Timer(HashMap<String, User> online){
               check = online;
       }

        public void run(){
                while(true){
                        long elapsed = System.currentTimeMillis();
                        Collection <User> all = check.values();
                        for(User u : all){
                                if ( elapsed - u.lastBeat() > TIMER)
                                        u.setBeat(false);
                        }
                }
                                
                

        }
}







