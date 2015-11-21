import java.util.*;
import java.net.*;
import java.io.*;

public class ServerThread implements Runnable {
        private int port;

        public ServerThread(int p){
                port = p;
        }

        public void run(){

                ServerSocket servSock; 
                MSCP mscp = new MSCP();

                //get port number and create listening socket
               try{
                        servSock = new ServerSocket(port);

                        while(true){
                                Socket clSock = servSock.accept();
                                BufferedReader br = new BufferedReader(new InputStreamReader(clSock.getInputStream()));
                                String request = br.readLine();
                                if (request.startsWith("login")){
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


