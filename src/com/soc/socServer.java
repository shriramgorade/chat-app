package com.soc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class socServer {
    public static void main(String[] args)throws Exception {
        int port = 9999;
        ServerSocket ss = new ServerSocket(port);

        //receive
        System.out.println("S: Server is waiting");
        Socket s = ss.accept();

        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String str = br.readLine();

        System.out.println("C: "+ str);

        //send
        OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());

        os.write("S: Hello from server\n");
        os.flush();

        s.close();
        ss.close();

    }
}
