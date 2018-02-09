package org.softuni.javache;
import org.softuni.javache.io.Reader;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ConnectionHandler extends Thread {
    private Socket clientSocket;

    private String cachedInputStreamContent;

    private InputStream clientSocketInputStream;

    private OutputStream clientSocketOutputStream;

    private Map<Integer, RequestHandler> requestHandlers;

    public ConnectionHandler(Socket clientSocket, Map<Integer, RequestHandler> requestHandlers) {
        this.initializeConnection(clientSocket);
        this.requestHandlers = requestHandlers;
    }

    private InputStream getClientSocketInputStream() throws IOException {
        if(this.cachedInputStreamContent == null){
            this.cachedInputStreamContent = Reader.readAllLines(this.clientSocketInputStream);
        }

        return new ByteArrayInputStream(this.cachedInputStreamContent.getBytes());
    }

    private void initializeConnection(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.clientSocketInputStream = this.clientSocket.getInputStream();
            this.clientSocketOutputStream = this.clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.processRequest();

            this.clientSocketInputStream.close();
            this.clientSocketOutputStream.close();
            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequest() throws IOException {

        for (RequestHandler requestHandler : this.requestHandlers.values()) {
            requestHandler.handleRequest(this.getClientSocketInputStream(), this.clientSocketOutputStream);

            if(requestHandler.hasIntercepted()){
                break;
            }

        }

    }
}






