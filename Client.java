import java.net.*;
import java.io.*;
import java.util.*;

public class Client{
        private static volatile boolean login = false;
        public static void main(String [] args){

                if(args.length != 2){
                        System.out.println("Usage: java Client <Server IP> <Port>\n");
                        System.exit(1);
                }

                try{
                        String servHost = args[0];                       //server IP
                        int servPort = Integer.parseInt(args[1]);        //server port
                        InetAddress addr = InetAddress.getLocalHost();
                        String host = addr.getHostAddress();             //client host
                        Scanner in = new Scanner(System.in);


                        SocketThread st;                                //thread to listen for messages
                        Heartbeat hb;                                   //heartbeat thread
                        HashMap <String, String> friends = new HashMap<String, String>();       //data structure for P2P messaging
                        int port = 0;                                                           //client port

                        /*
                         * User authentication; call authenticate() in a loop until a valid userID is established.
                         * The user can't send any commands until she logs in, so do this before command prompt.
                         */

                       
                        String user = null;
                        while(!login){
                                user = authenticate(servHost, servPort, host);
                                if( user != null){
                                        login = true;
                                }                  
                        }

                        //at this point, user logged in so begin listening for messages on a port
                        st = new SocketThread();
                        st.start();

                        //wait until port is established to get port number and begin connections
                        while(port == 0)
                                port = st.getPort();
                        String command = "register " + user + " " + host + " " + port;
                        send(servHost, servPort, command);         
                        //start sending heartbeats
                        hb = new Heartbeat(user, servHost, servPort);
                        hb.start();

                        //let validation messages print before starting command prompt
                        try{
                                Thread.sleep(10); 
                        }
                        catch(InterruptedException ie){};

           
                       
                        while(login){

                                System.out.print(">");                         
                                command = in.nextLine();
                                command += " " + user;
                                command.toLowerCase();

                                //private message commands don't go to the server so we need to intercept these
                                if(command.startsWith("private")){
                                        privateMessage(command, friends);

                                }

                                //a getaddress command will require us storing information locally
                                else if (command.startsWith("getaddress")){
                                        send(servHost, servPort, command);
                                        friends = st.getUpdatedFriendList();
                                }
                                //otherwise everything is between the server and user
                                else{
                                        send(servHost, servPort, command);
                                }

                                //end loop and program if user types logout
                                login = !command.startsWith("logout");

                        }


                }

                catch(IOException ioe){System.err.println("Could not establish a connection.");}
        }

        //private message method looks up local copy of friend IP addresses  and sends them directly to the peer
        private static void privateMessage(String request, HashMap <String, String> friends){
                String[] line = request.split("\\s+");
                String buddy  = line[2];
                String from = line[line.length-1];
                if(friends.get(buddy) == null){
                        System.out.println("This user is not currently your friend. Request their address with <getaddress> and try again.");
                }
                String [] friend = friends.get(buddy).split("\\s+");

                //else{
                String msg = "New private message from: " + from;
                msg += "\n Message: " + 
                        request.substring(request.indexOf(line[2]), request.lastIndexOf(from));
                send(friend[0], Integer.parseInt(friend[1]), msg);
                // }
        }

        //auxiliary method to send messages to server
        private static void send(String servIP, int servPort, String msg){
                try{
                        Socket s = new Socket(servIP, servPort);
                        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                        pw.println(msg);
                        s.close();
                }
                catch(IOException ioe){};
        } 
        //quick method for printing
        private static void p(String print){
                System.out.print(print);
        }

        //authentication protocol.
        private static String authenticate(String servHost, int serverPort, String clientHost){
                Scanner in = new Scanner(System.in);
                String request = "login " + clientHost + " ", response;          
                String user = null;
                try{
                        p("Enter Username: ");
                        user = in.nextLine();
                        request += " " + user + " ";
                        p("Enter Password: ");
                        request +=  in.nextLine();
                        Socket s = new Socket(servHost, serverPort);
                        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        System.err.println("Sending request: " + request);
                        pw.println(request);
                        response = br.readLine();
                        while(response == null)
                                response = br.readLine();
                        if(!response.startsWith("Welcome"))
                                user = null;    
                        p(response);
                        //close socket
                        s.close();

                }
                catch(IOException ioe){ioe.printStackTrace();}

                return user;
        }

}
