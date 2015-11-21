/*
 * This is the Messaging Service Center Protocol class that generally takes in a request line and
 * fires off the appropriate response. It keeps track for who is online and stores the credentials.
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class MSCP{ 
        private HashMap<String, User> creds = new HashMap<String, User>();
        private HashMap<String, User> online = new HashMap<String, User>();
        private final long TIMEOUT = 60000;                     //log-in timeout for multiple attempts


        public MSCP(){
                //read in credentials
                try{
                        File f = new File("credentials.txt");
                        Scanner read = new Scanner(f);
                        while (read.hasNextLine()){
                                String name = read.next();
                                String pw = read.next();
                               creds.put(name, new User(name, pw));
                        }

                }

                catch(IOException e){System.err.println("There was a fatal exception; log-in credentials could not be read.");}
        }
        
        //take in a request, look at the first word and call the appropriate method
        public void handleRequest(String requestLine){
                if (requestLine == null)
                        return;
                String [] line = requestLine.split("\\s+");
                String key = line[0].toLowerCase();
                User u = creds.get(line[line.length-1]);

                if (key.equals("login"))
                        login(requestLine);

                else if (key.equals("logout"))
                        logout(line);

                else if (key.equals("broadcast")){
                        String from = line[line.length-1];
                        broadcast(from, line[1], requestLine);
                }

                else if (key.equals("message")){
                        String to = line[1];
                        String first = line[2];                      
                        message(u.getName(), to, first, requestLine);
                }

                else if(key.equals("getaddress")){
                        String asker = u.getName();
                        String peer = line[1];
                        getAddress(asker, peer);
                }

                else if (key.equals("online"))
                        online(u);

                else if (key.equals("register"))
                        register(line);

                else if (key.equals("heartbeat")){
                        u = online.get(line[1]);
                        if (u != null){
                                u.resetBeat(Long.parseLong(line[2]));
                        }
                }

                else if (key.equals("block")){
                        String blocker = line[2];
                        String blocked = line[1];
                        block(blocker, blocked);
                }


                else{
                        System.err.println("Request not understood");
                        send(u.getIP(), u.getPort(), "Invalid request");
                }                        
        }


                
        //send IP address back to user
        private void getAddress(String peer, String toPeer){
                User sendTo = creds.get(peer);
                User friend = creds.get(toPeer);
                if(sendTo.isBlockedBy(friend)){
                        send(sendTo.getIP(), sendTo.getPort(), "You have been blocked by this user.\n>");
                        return;
                }

                String response = "IP_REQUEST " + toPeer + " " + friend.getIP() + " : " + friend.getPort();
                sendTo.p2p(friend);
                send(sendTo.getIP(), sendTo.getPort(), response);
        }


       //blocking and unblocking are basically and removing a maintained blacklist for each user
        private void block(String blocker, String blocked){
                User b1 = creds.get(blocked);
                User b2 = creds.get(blocker);
                b1.block(b2); 
        }

        private void unblock(String blocker, String blocked){
                User b1 = creds.get(blocked);
                User b2 = creds.get(blocker);
                b1.unblock(b2);
        }


        private void online(User u){
                String response = "Currently online: " + online.keySet() + "\n>";
                send(u.getIP(), u.getPort(), response);
        }

        /*
         * Login protocol takes a string of the format "login <clientIP> <username> <password>"
         */

        public String login(String requestLine){
                String[] request = requestLine.split("\\s+");
                String clientIP = request[1];
                String username = request[2];
                String pw = request[3];
                User user = creds.get(username);
                String send = "";

                if(user == null)
                        send = "Not a registered user. ";

                else if (user.getStatus().equals("timeout") && 
                                System.currentTimeMillis() < TIMEOUT + user.getTimeout() )
                        send = "Your account has been temporarily blocked. Please try again after some time has passed. ";
                else if (user.isPassword(pw)){
                        String currentStatus = user.getStatus();
                        if (currentStatus.equals("login"))
                                send(user.getIP(), user.getPort(), "You have been logged out by someone else.");
                        user.setStatus(1);
                        Collection<User> all = online.values(); 
                        for (User u : all )
                                send(u.getIP(), u.getPort(), user.getName() + " has logged in.\n>" );


                        online.put(username, user);
                        send = "Welcome,\n " + user.getName() + ".\n>";
                                       }
                else if (user.getAttempts() < 3){
                        user.incAttempts();
                        send = "Invalid credentials.";
                }

                else{
                        user.setTimeout(System.currentTimeMillis());
                        user.setStatus(2);
                        user.resetAttempts();
                        send = "Too many failed attempts. You are temporarily blocked from logging in.";
                }
                return send;             


        }

        //This method registers a users listening port with the server so it can receive message

        private void register(String [] request){
                User u = creds.get(request[1]);
                u.setIP(request[2]);
                u.setPort(Integer.parseInt(request[3]));
                online.put(u.getName(), u);
                send(u.getIP(), u.getPort(), "Login successful!\n");
                //send any queued messages while we're at it
                while(!u.isEmptyQueue()){
                        send(u.getIP(), u.getPort(), u.popQueue());
                }
		send(u.getIP(), u.getPort(),"\n>");

        }

        
        private String logout(String[] request){
                User user = creds.get(request[request.length-1]);
                user.setStatus(0);
                online.remove(user.getName());
                //send this logout signal to client listener so it can shutdown
                send(user.getIP(), user.getPort(), "logout");
                //broadcast user logged out
                Collection<User> all = online.values();
                for (User u : all )
                        send(u.getIP(), u.getPort(), user.getName() + " has logged out." );
                return "Goodbye!";
        }

        private void broadcast(String from, String first, String request){
                Set<String> all = online.keySet();
                for(String user : all){
                        String msg = "broadcast" + user + " " + request.substring(request.indexOf(first));
                        message(from, user, first, msg);
                }
        }

        //message method
        private String message(String from, String to, String first, String msg){
                User sendTo = creds.get(to);
                User sender = creds.get(from);
                if (sendTo == null)
                        return "Unknown user";
                else if(from.equals(to))        //if user is sending messaging to self, return
                        return "";
                else if(sender.isBlockedBy(sendTo))   //check if user is blocked
                        send(sender.getIP(), sender.getPort(), "Your message could not be delivered to a recipient because they have blocked you.");
                else if(!online.containsKey(to)){    //queue a message if the user isn't online but is a valid user
                        sendTo.queue("From: " + from + "\nMessage: " + msg.substring(msg.indexOf(first), msg.lastIndexOf(from))+"\n>");
                }               

                else{   //send directly if valid, unblocked, online user
                        String sendString = "From: " + from + "\nMessage: " + msg.substring(msg.indexOf(first), msg.lastIndexOf(from));
                       send(sendTo.getIP(), sendTo.getPort(), sendString+"\n>");
                }
                return "Message sent.";
        }

        //generic send method
        private void send(String IP, int p, String msg){
                try{
                        Socket s = new Socket(IP, p);
                        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                        pw.println(msg);
                        s.close();
                }
                catch(Exception e){System.err.println("There was an error sending a message from the server.");}
        }




}
