import java.net.*;
import java.io.*;
import java.util.*;

public class Server{
        public static void main(String [] args){
                ServerSocket servSock;
                int port;   
                MSCP mscp = new MSCP();

                //check for appropriate command line args
                if (args.length != 1){
                        System.out.println("Usage: java Server <portnum>");
                        System.exit(1);
                }

                //get port number and create listening socket
                port = Integer.parseInt(args[0]);

                try{
                        servSock = new ServerSocket(port);

                        while(true){
                                Socket clSock = servSock.accept();
                                BufferedReader br = new BufferedReader(new InputStreamReader(clSock.getInputStream()));
                                String request = br.readLine();
                                if (request!= null && request.startsWith("login")){
                                        PrintWriter pw = new PrintWriter(clSock.getOutputStream(), true);
                                        String temp = mscp.login(request);
                                        System.err.println(temp);
                                        pw.println(temp);
                                        clSock.close();

                                }
                                else{
                                        clSock.close();
                                        mscp.handleRequest(request);
                                }



                                System.err.println("Processing request: " + request);                               

                        }

                }
                catch(IOException ioe){
                        ioe.printStackTrace();
                }

        }



}

