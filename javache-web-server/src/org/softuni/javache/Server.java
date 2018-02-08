package org.softuni.javache;

import org.softuni.javache.http.HttpSessionStorageImpl;
import org.softuni.javache.http.HttpSessionStorage;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final String LISTENING_MESSAGE = "Listening on port: ";

    private static final String TIMEOUT_DETECTION_MESSAGE = "Timeout detected!";

    private static final Integer SOCKET_TIMEOUT_MILLISECONDS = 5000;

    private int port;

    private int timeouts;

    private ServerSocket server;

    private Map<Integer, RequestHandler> requestHandlers;

    public Server(int port) {
        this.port = port;
        this.timeouts = 0;
        this.requestHandlers = requestHandlers;
        this.startLoadingProcess();
    }

    private void startLoadingProcess() {
        //ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        this.initializeRequestHandlers();


        /*exec.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                System.out.println("Loaded handlers.");

            }
        }, 0, 10, TimeUnit.SECONDS);*/


    }

    private void initializeRequestHandlers(){
        ServerConfig serverConfig = new ServerConfig(WebConstants.WEB_SERVER_ROOT_FOLDER_PATH);

        this.requestHandlers = new RequestHandlerLoader(serverConfig)
                .loadRequestHandlers();
    }

    public void run() throws IOException {
        this.server = new ServerSocket(this.port);
        System.out.println(LISTENING_MESSAGE + this.port);

        this.server.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

        HttpSessionStorage sessionStorage = new HttpSessionStorageImpl();

        while(true) {
            try(Socket clientSocket = this.server.accept()) {
                clientSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

                ConnectionHandler connectionHandler
                        = new ConnectionHandler(clientSocket, this.requestHandlers);

                FutureTask<?> task = new FutureTask<>(connectionHandler, null);
                task.run();
            } catch(SocketTimeoutException e) {
                System.out.println(TIMEOUT_DETECTION_MESSAGE);
                this.timeouts++;
            }
        }
    }
}