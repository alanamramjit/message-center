import java.util.*;

public class User{
        private String name;
        private String password;
        private String status;
        private int loginAttempts;
        private String IP;
        private int port;
        private ArrayList<String> blockedBy;
        private long timeout;
        private ArrayList<String> queue;
        private HashMap<String, User> p2p;
        private boolean heartbeat;
        private long lastbeat;
        private final String [] STATUS_CODES = {"logout", "login", "timeout"};

        public User(String username, String pw){
                name = username;
                password = pw;
                status = STATUS_CODES[0];
                loginAttempts = 0;
                IP = null;
                port = 0;
                blockedBy = new ArrayList<String>();
                timeout =  0;
                p2p = new HashMap<String, User>();
                queue = new ArrayList<String>();
                heartbeat = true;
        }
        public void setBeat(boolean hb){
                heartbeat = hb;
        }

        public boolean heartbeat(){
                return heartbeat;
        }

        public void resetBeat(long time){
                lastbeat = time;
        }

        public long lastBeat(){
                return lastbeat;
        }

        public void setTimeout(long time){
                timeout = time;
        }

        public void p2p(User u){
                p2p.put(u.getName(), u);
        }

        public void queue(String msg){
                queue.add(msg);
        }

        public boolean isEmptyQueue(){
                return queue.size() == 0;
        }

        public String popQueue(){
                        int lastMessage = queue.size() -1;
                        String message = queue.get(lastMessage);
                        queue.remove(lastMessage);
                        return message;
        }



        public long getTimeout(){
                return timeout;
        }

        public String getName(){
                return name;
        }

        public boolean isPassword(String guess){
                return guess.equals(password);
        }

        public boolean isBlockedBy(User u){
               return blockedBy.contains(u.getName());
        }

        public void block(User u){
                if(!blockedBy.contains(u.getName()))
                        blockedBy.add(u.getName());
       }

        public void unblock(User u){
                blockedBy.remove(u.getName());
        }

        public String getStatus(){
                return status;
        }

        public void incAttempts(){
                loginAttempts++;
        }

        public void resetAttempts(){
                loginAttempts = 0;
        }

        public int getAttempts(){
                return loginAttempts;
        }

        public String getIP(){
                return IP;
        }

        public void setIP(String newIP){
                IP = newIP;
        }

        public int getPort(){
                return port;
        }

        public void setPort(int p){
                port = p;
        }

        public void setStatus(int statusCode){
                status = STATUS_CODES[statusCode];
        }
                
}


