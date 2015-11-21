import java.net.*;
import java.util.*;
import java.io.*;

public class SocketThread extends Thread{
        private ServerSocket listen;
        private int port;
        private HashMap <String, String> friendList = new HashMap<String, String>();
        public void run(){
                try{
                        listen = new ServerSocket(0);
                        port = listen.getLocalPort();
                        while(true){

                                Socket call = listen.accept();
                                BufferedReader br = new BufferedReader(new InputStreamReader(call.getInputStream()));
                                String message = "";
                                String line = "";
                                while(line != null){
                                        message += line;
                                        line = br.readLine();
                                        message += "\n";
                                }                                        
                                if(message.contains("IP_REQUEST ")){
        
                                        addFriend(message);
                               }

                               else if(message.contains("logout")){
                                       System.out.println("Goodbye!");
                                               System.exit(1);
                               }

                                else                                        
                                 System.out.println(message);
                                //System.out.println(br.readLine());
                                call.close();
                        }
                }
                catch(IOException ioe){System.out.println("There was an error printing messages");}
        }
        public HashMap<String, String> getUpdatedFriendList(){
                return friendList;
        }

        public void addFriend(String message){
                String [] line = message.split("\\s+");
                friendList.put(line[2], line[3] +" " + line [5]);
                System.err.println(line[2] + " added at " + line[3] +":" + line[5]);
        }
                

        public int getPort(){
                return port;
        }

        public void shutdown(){
                if(listen != null)
                        try{
                        listen.close();
                        }
                catch(IOException ioe){}
                System.exit(1);
        }

}




