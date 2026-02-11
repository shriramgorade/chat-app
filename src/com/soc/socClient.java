package com.soc;

import java.io.OutputStreamWriter;
import java.net.Socket;

public class socClient {
    public static void main(String[] args)throws Exception {
        String ip = "localhost";
        int port = 9999;
        Socket s = new Socket(ip, port);

        OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
        os.write("Hello\n");
        os.flush();
        os.close();
        s.close();
    }

}
