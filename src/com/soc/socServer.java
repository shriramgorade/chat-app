package com.soc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class socServer {
    public static void main(String[] args)throws Exception {
        int port = 9999;
        ServerSocket ss = new ServerSocket(port);

        System.out.println("Server is waiting");
        Socket s = ss.accept();

        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String str = br.readLine();

        System.out.println("Client data:"+ str);
        br.close();
        s.close();
        ss.close();

    }
}
