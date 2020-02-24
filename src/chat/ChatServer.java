package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class ChatServer {


    private static final int PORT = 9001;


    private static HashMap<String, PrintWriter> writers = new HashMap<>();


    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    public static void sendClientList() {
    	String clientList = "";
    	for (String client : writers.keySet()) {
            clientList = clientList + client + "\b";
        }
    	for (PrintWriter writer : writers.values()) {
            writer.println("CLIENTS " + clientList);
        }
    }


    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        
        private int index;
        private String receiver;
        private PrintWriter receiverPW;
        public Handler(Socket socket) {
            this.socket = socket;
        }


        public void run() {
            try {


                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);


                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (writers) {
                        if (!writers.containsKey(name)) {
                        	writers.put(name, out);
                        	sendClientList();
                            break;
                        }
                    }
                }


                out.println("NAMEACCEPTED");

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    if((index=input.indexOf(">>")) != -1) {
                    	receiver = input.substring(0, index);
                    	if(writers.containsKey(receiver)) {
                    		receiverPW = writers.get(receiver);
                    		receiverPW.println("MESSAGE " + name + ": " + input.substring(index+2));
                    		out.println("MESSAGE " + name + "->" + receiver + ": " + input.substring(index+2));
                    	}
                    	else {
                    		out.println("MESSAGE Error Sending Message. Client " + receiver + " Is Not Connected.");
                    	}
                    }
                    else {
	                    for (PrintWriter writer : writers.values()) {
	                        writer.println("MESSAGE " + name + ": " + input);
	                    }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {

                if (name != null) {
                	writers.remove(name);
                	sendClientList();
                }

                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}