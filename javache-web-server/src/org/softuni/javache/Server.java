package org.softuni.javache;

import org.softuni.javache.http.HttpSessionStorageImpl;
import org.softuni.javache.http.HttpSessionStorage;
import org.softuni.javache.util.RequestHandlerLoader;

import java.io.*;
import java.net.*;
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

    private RequestHandlerLoader requestHandlerLoader;

    public Server(int port) {
        this.port = port;
        this.timeouts = 0;
        this.requestHandlerLoader = new RequestHandlerLoader();
        this.startLoadingProcess();
    }

    private void startLoadingProcess() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        exec.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                System.out.println("Loaded handlers.");
                initializeRequestHandlers();

            }
        }, 0, 10, TimeUnit.SECONDS);


    }

    private void initializeRequestHandlers(){
        this.requestHandlerLoader.loadRequestHandlers();
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
                        = new ConnectionHandler(clientSocket,
                        this.requestHandlerLoader.getLoadedRequestHandlers());

                FutureTask<?> task = new FutureTask<>(connectionHandler, null);
                task.run();
            } catch(SocketTimeoutException e) {
                System.out.println(TIMEOUT_DETECTION_MESSAGE);
                this.timeouts++;
            }
        }
    }
}