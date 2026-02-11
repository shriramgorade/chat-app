package com.soc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class socClient {
    public static void main(String[] args)throws Exception {
        String ip = "localhost";
        int port = 9999;
        Socket s = new Socket(ip, port);

        //send
        OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
        os.write("Hello from client\n");
        os.flush();
        System.out.println("Data sent");


        //receive
        System.out.println("Client is waiting");
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String str = br.readLine();
        System.out.println(str);

        s.close();
    }

}
