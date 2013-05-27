package net.damian.wojdat.talktalk_server;

import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_REQ_USR_NAME;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_SET_ID;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_SRV_DISCONNECT;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_SRV_MSG;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_SRV_PING;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_USR_LST_UPDATE;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TalkTalkClientThread extends Thread{
	
	private Socket socket = null;
	private Integer id = null;
	String name = null;
	private ObjectOutputStream sockObjOut = null;
	private ObjectInputStream sockObjIn = null;
	private Timer connectionCheckTimer = null;
	
	public TalkTalkClientThread(Socket socket) throws IOException {
		
		super("TalkTalkClientThread");
		this.socket = socket;
		
		sockObjOut = new ObjectOutputStream(socket.getOutputStream());		
		sockObjOut.flush();
		sockObjIn = new ObjectInputStream(socket.getInputStream());
		
		TalkTalkServer.messagesService.sendMessage(-1, "[SERVER:] New client connected from " + socket.getInetAddress(), CMD_SRV_MSG);
		
		if(TalkTalkServer.getClientCount() >= TalkTalkServer.maxClients) {
			sendMessageToClientFromServer(CMD_SRV_MSG + " Maxim connections reached!");
			sendMessageToClientFromServer(CMD_SRV_DISCONNECT + " ");	
			sockObjOut.close();
			sockObjIn.close();
			socket.close();
		}

		sendMessageToClientFromServer(CMD_SRV_MSG + " Receiving ID...");
		
		for(Integer i = 0; i < TalkTalkServer.maxClients; i++) {
			if(TalkTalkServer.clientsList.containsKey(i))
				continue;
			else {
				TalkTalkServer.clientsList.put(i, this);
				id = i;				

				sendMessageToClientFromServer(CMD_SET_ID + " " + id);
				sendMessageToClientFromServer(CMD_SRV_MSG + " ID received !");
				
				getUserName();
				
				break;
			}
		}
		
	}
	
	Object obj = null;
	String str = null;
	
	public void run() {
		
		ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();	
		worker.schedule(new Runnable() {
			@Override
			public void run() {
				setConnectionChecker();
			}}, 2, TimeUnit.SECONDS);
		
		try {
			while(!socket.isClosed()) {
				obj = sockObjIn.readObject();
				if(obj instanceof String && obj != null) {
					str = (String) obj;
					if(str == null) 
						break;
					TalkTalkServer.messagesService.sendMessage(id, str);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	void disconnectUser() {
		
		cancellConnectionChecker();
		
		try {
			this.close();
		} catch (IOException e) {}		
		
		TalkTalkServer.clientsList.remove(id);
		TalkTalkServer.clientsNames.remove(id);
		
		for(Integer key: TalkTalkServer.clientsList.keySet()) {
			TalkTalkServer.clientsList.get(key).sendNewUserList();
		}
		
		TalkTalkServer.messagesService.sendMessage(-1, "[SERVER:] Client [" + id + "] disconnected from the server", CMD_SRV_MSG);
		System.out.println("[INFO:][SERVER:] Client [" + id + "] disconnected from the server");
	
	}
	
	public void receiveMessage(String message) {
		
		try {
			sockObjOut.writeObject(message);
			sockObjOut.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void getUserName() {
		
		System.out.println("[DEBUG:] getUserName");
		sendMessageToClientFromServer(CMD_REQ_USR_NAME + " ");
		
	}
	
	public void sendNewUserList() {
		
		System.out.println("[DEBUG:] sendNewUserList, id = " + id + ", users = " + TalkTalkServer.clientsNames);
		try {
			sendMessageToClientFromServer(CMD_USR_LST_UPDATE + " ");
			sockObjOut.writeObject(TalkTalkServer.clientsNames);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	public void close() throws IOException {
		socket.close();
	}
	
	public Integer getClientId() {
		return id;
	}

	public void sendMessageToClientFromServer(String message) {
		System.out.println("[DEBUG:] sendMessageToClientFromServer");
		receiveMessage(message);
	}
	
	private void setConnectionChecker() {
		if(connectionCheckTimer != null) {
			connectionCheckTimer.cancel();
		}
		
		class connectionChecker extends TimerTask {
			@Override
			public void run() {
				try {
					sockObjOut.writeObject(CMD_SRV_PING + " ");
				} catch (IOException e) {
					this.cancel();
					disconnectUser();
				}		
			}
		}
		
		connectionCheckTimer = new Timer();
		connectionCheckTimer.schedule(new connectionChecker(), 0, 2000);		
	}	
	
	private void cancellConnectionChecker() {
		connectionCheckTimer.cancel();
	}
	
}
