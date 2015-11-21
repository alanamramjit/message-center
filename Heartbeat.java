/**
 * Heartbeat thread keeps track of the last time it sent a beat and sends one once
 * every BEAT_MS miliseconds. Straightforwad implentation.
 */

import java.net.*;
import java.util.*;
import java.io.*;

public class Heartbeat extends Thread{
        private long lastBeat;
        private final long BEAT_MS = 30000;
        private String serverIP, userID;
        private int serverPort;

        public Heartbeat(String ID, String sip, int sp){
                userID = ID;
                serverIP = sip;
                serverPort = sp;
        }

        public void run(){
                lastBeat = System.currentTimeMillis();
                while (true){
                        long currTime = System.currentTimeMillis();
                        if(lastBeat + BEAT_MS <= currTime){
                                try{
                                        Socket s = new Socket(serverIP, serverPort);
                                        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                                        pw.write("heartbeat " + userID + currTime);
                                        s.close();
                                }

                                catch(IOException ioe){}
                                lastBeat = currTime;
                        }

                }

        }
}



