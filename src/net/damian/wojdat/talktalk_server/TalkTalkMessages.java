package net.damian.wojdat.talktalk_server;

import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_CLT_DISCONNECT;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_CLT_PING;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_GIVE_USR_NAME;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_MSG;
import static net.damian.wojdat.talktalk_server.TalkTalkCommands.CMD_SRV_MSG;

public class TalkTalkMessages {
	// TODO: Store messages by date (Calendar class)
	
	public void sendMessage(Integer fromId, String message, String command) {
		
		Integer strIdx = null;
		
		System.out.println("[INFO:][MESSAGE:][" + fromId + ":] " + message);		
		
		if(command == "") {
			if((strIdx = message.indexOf(" ")) != -1) {
				command = message.substring(0, strIdx);					
			}
			else {
				if(fromId == -1) {
					command = CMD_SRV_MSG;
				}
				else {
					command = CMD_MSG;
				}
			}
		}
		
		switch(command) {
			case CMD_MSG: {
				for(Integer key: TalkTalkServer.clientsList.keySet()) {
					if(key != fromId)
						TalkTalkServer.clientsList.get(key).receiveMessage(message);
				}
				break;
			}
			case CMD_SRV_MSG: {
				for(Integer key: TalkTalkServer.clientsList.keySet()) {
					TalkTalkServer.clientsList.get(key).sendMessageToClientFromServer(CMD_SRV_MSG + " " + message);
				}
				break;
			}
			case CMD_CLT_DISCONNECT: {
				TalkTalkServer.clientsList.get(fromId).disconnectUser();
				break;
			}
			case CMD_GIVE_USR_NAME: {
				System.out.println("[DEBUG:] CMD_GIVE_USR_NAME, name = " + message.substring(strIdx+1));
				TalkTalkServer.clientsNames.put(fromId, message.substring(strIdx+1));
				TalkTalkServer.clientsList.get(fromId).name = message.substring(strIdx+1);
				for(Integer key: TalkTalkServer.clientsList.keySet()) {
					TalkTalkServer.clientsList.get(key).sendNewUserList();
				}
				break;
			}
			case CMD_CLT_PING: {
				System.out.println("[DEBUG:] CMD_CLT_PING, id = " + fromId);
				break;
			}
		}	
	}
	
	public void sendMessage(Integer clientId, String message) {
		sendMessage(clientId, message, "");
	}
}
