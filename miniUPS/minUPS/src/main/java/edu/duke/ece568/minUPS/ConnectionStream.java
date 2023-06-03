package edu.duke.ece568.minUPS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionStream {
    public Socket socket;
    public InputStream inputStream;
    public OutputStream outputStream;

    public ConnectionStream(Socket socket)throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public void close()throws IOException{
        socket.close();
    }
}
