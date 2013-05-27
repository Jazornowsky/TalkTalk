package net.damian.wojdat.talktalk_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class TalkTalkServer {
	
	public static final HashMap<Integer, TalkTalkClientThread> clientsList = new HashMap<Integer, TalkTalkClientThread>();
	public static final HashMap<Integer, String> clientsNames = new HashMap<Integer, String>();
	public static final TalkTalkMessages messagesService = new TalkTalkMessages();
	static final Integer maxClients = 5;
	static ServerSocket serverSocket = null;
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("[SERVER:] TalkTalk server launching...");
		
		Boolean listening = true;
	     
		try {
	    	serverSocket = new ServerSocket(4444);
	    } catch (IOException e) {
	    	System.err.println(e);
	        System.exit(1);
	    }
	
		System.out.println("[SERVER:] Binded to port " + serverSocket.getLocalPort());
		System.out.println("[SERVER:] Listening for incomming connections...");
		 
	    while(listening) {
	    	new TalkTalkClientThread(serverSocket.accept()).start();
	    }
	    	 
	    	 
	    serverSocket.close();
	 
	}
	
	public static Integer getClientCount() {
		return clientsList.size();
	}
	
	/*public static void disconnectUser(Integer id) throws IOException {
		
		TalkTalkServer.clientsList.get(id).receiveMessage(id, TalkTalkCommands.CMD_SRV_DISCONNECT + " ");
		TalkTalkServer.clientsList.get(id).close();		
		TalkTalkServer.clientsList.remove(id);
		TalkTalkServer.clientsNames.remove(id);
		
		for(Integer key: TalkTalkServer.clientsList.keySet()) {
			TalkTalkServer.clientsList.get(key).sendNewUserList();
		}
		
		TalkTalkServer.messagesService.sendMessageFromClient(-1, "/msg [SERVER:] Client [" + id + "] disconnected from the server");
		System.out.println("[INFO:][SERVER:] Client [" + id + "] disconnected from the server");*/
		
		
		/*class removeUserTask implements Runnable {
			Integer id;
			removeUserTask(Integer _id) { id = _id; }
			public void run() {
				removeUser(id);
			}			
		}
		
		new Thread(new removeUserTask(id)).start();*/
	//}
	
	/*private static void removeUser(Integer id) {
		TalkTalkServer.clientsList.remove(id);
		TalkTalkServer.clientsNames.remove(id);
		TalkTalkServer.messagesService.sendMessageFromClient(-1, "/msg [SERVER:] Client [" + id + "] disconnected from the server");
		System.out.println("[INFO:][SERVER:] Client [" + id + "] disconnected from the server");
		for(Integer key: TalkTalkServer.clientsList.keySet()) {
			TalkTalkServer.clientsList.get(key).sendNewUserList();
		}
	}*/
}
