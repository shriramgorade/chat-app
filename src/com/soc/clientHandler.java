package com.soc;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class clientHandler implements Runnable{

    //can use ArrayList as well, CopyOnWriteArraySet is used for thread safe operations.
    public static CopyOnWriteArraySet<clientHandler> clientHandlers = new CopyOnWriteArraySet<>();
    //groups: groupName -> members (ConcurrentHashMap is thread safe)
    public static ConcurrentHashMap<String, CopyOnWriteArraySet<clientHandler>> groups = new ConcurrentHashMap<>();
    //groups this client has joined
    private CopyOnWriteArraySet<String> joinedGroups = new CopyOnWriteArraySet<>();
    private Socket socket;
    private BufferedReader bufferedReader;  //receive data
    private BufferedWriter bufferedWriter;  //send data
    private String clientUsername;

    public clientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter((new OutputStreamWriter(socket.getOutputStream())));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server: "+ clientUsername + " has joined the chat!");
            sendLocalMessage("Server: type /help for group commands");
        } catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run(){
        String messageFromClient;

        while(true){
            try{
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient == null) break; //client disconnected

                if(messageFromClient.startsWith("/")){
                    handleCommand(messageFromClient);
                }else if(messageFromClient.startsWith("@")){
                    int firstSpace = messageFromClient.indexOf(" ");
                    if(firstSpace > 1){
                        String group = messageFromClient.substring(1, firstSpace);
                        String msg = messageFromClient.substring(firstSpace+1);
                        broadcastToGroup(group, clientUsername + "(group " + group + "):" + msg);
                    }else{
                        sendLocalMessage("Server: Invalid group message. Use @groupName message");
                    }
                }else{
                    broadcastMessage(clientUsername + ": " + messageFromClient);
                }
            } catch (IOException e) {
                break; //closeEverything() called below
            }
        }
        closeEverything(socket, bufferedReader, bufferedWriter);
    }

    public void broadcastMessage(String messageToSend){
        for(clientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                clientHandler.closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }

    public void sendLocalMessage(String message){
        try{
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        //remove from all groups
        for(String grp : joinedGroups){
            CopyOnWriteArraySet<clientHandler> members = groups.get(grp);
            if(members != null) {
                members.remove(this);
                if (members.isEmpty()) groups.remove(grp);
            }
        }
        broadcastMessage("Server: "+ clientUsername + "has left the chat!");
    }

    private void handleCommand(String cmdLine){
        String[] parts = cmdLine.split(" ", 3);
        String cmd = parts[0].toLowerCase();

        switch(cmd){
            case "/help":
                sendLocalMessage("Commands: /create <group> | /join <group> | /leave <group> | list | /g <group> <message> or @group <message> | /quit");
                break;
            case "/create":
                if(parts.length < 2) sendLocalMessage("Invalid. Use: /create <groupName>");
                else createAndJoinGroup(parts[1]);
                break;
            case "/join":
                if(parts.length < 2) sendLocalMessage("Invalid. Use: /join <groupName>");
                else joinGroup(parts[1]);
                break;
            case "/leave":
                if(parts.length < 2) sendLocalMessage("Invalid. Use: /leave <groupName>");
                else leaveGroup(parts[1]);
                break;
            case "/list":
                listGroups();
                break;
            case "/g":
                if(parts.length < 3) sendLocalMessage("Invalid. Use: /g <groupName> <message>");
                else broadcastToGroup(parts[1], clientUsername + " (group " + parts[1] + "): " + parts[2]);
                break;
//            case "/quit":
//                handled locally in client.sendMessage
            default:
                sendLocalMessage("Server: Unknown command. Type /help");

        }
    }

    private void createAndJoinGroup(String groupName){
        groups.computeIfAbsent(groupName, k -> new CopyOnWriteArraySet<>());
        groups.get(groupName).add(this);
        joinedGroups.add(groupName);

        broadcastMessage("Server: " + clientUsername + "has created and joined group " + groupName + "!");

    }

    private void joinGroup(String groupName){
        groups.computeIfAbsent(groupName, k -> new CopyOnWriteArraySet<>());
        groups.get(groupName).add(this);
        joinedGroups.add(groupName);
        broadcastMessage("Server: " + clientUsername + "has joined group " + groupName + "!");

    }

    private void leaveGroup(String groupName){
        CopyOnWriteArraySet<clientHandler> members = groups.get(groupName);
        if(members == null || !members.contains(this)){
            sendLocalMessage("Server: You are not a member of " + groupName);
            return;
        }
        members.remove(this);
        joinedGroups.remove(groupName);
        broadcastMessage("Server: " + clientUsername + " has left group " + groupName + "!");
        if(members.isEmpty()) groups.remove(groupName);
    }

    private void listGroups(){
        StringBuilder sb = new StringBuilder();
        sb.append("Server: All groups: ");
        sb.append(groups.isEmpty() ? "None" : groups.keySet());
        sb.append("\nServer: Your groups: ").append(joinedGroups.isEmpty() ? "None" : joinedGroups);
        sendLocalMessage(sb.toString());
    }

    private void broadcastToGroup(String groupName, String message){
        CopyOnWriteArraySet<clientHandler> members = groups.get(groupName);
        for(clientHandler member : members){
            try{
                member.bufferedWriter.write(message);
                member.bufferedWriter.newLine();
                member.bufferedWriter.flush();
            }catch (IOException e){
                member.closeEverything(member.socket, member.bufferedReader, member.bufferedWriter);
            }
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
