package com.soc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    private ServerSocket serverSocket;

    public server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("New user has joined the chat!");
                clientHandler clientHandler = new clientHandler(socket);

                Thread thread = new Thread(clientHandler);
            }
        }catch(IOException e){

        }
    }


    public void closeServer(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket socket = new ServerSocket(9999);
        server server = new server(socket);
        server.startServer();
    }
}
