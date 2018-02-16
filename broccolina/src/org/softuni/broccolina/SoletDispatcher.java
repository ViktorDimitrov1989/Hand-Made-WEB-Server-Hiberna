package org.softuni.broccolina;

import org.softuni.broccolina.solet.*;
import org.softuni.broccolina.util.ApplicationLoader;
import org.softuni.javache.RequestHandler;
import org.softuni.javache.io.Reader;
import org.softuni.javache.io.Writer;

import java.io.*;

public class SoletDispatcher implements RequestHandler {

    private final String SERVER_ROOT_PATH;

    private boolean intercepted;

    private ApplicationLoader applicationLoader;

    public SoletDispatcher(String serverRootPath) {
        this.SERVER_ROOT_PATH = serverRootPath;
        this.intercepted = false;
        this.applicationLoader = new ApplicationLoader(SERVER_ROOT_PATH);
        this.applicationLoader.loadApplications();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            String requestContent = new Reader().readAllLines(inputStream);

            HttpSoletRequest request = new HttpSoletRequestImpl(requestContent, null);
            HttpSoletResponse response = new HttpSoletResponseImpl(outputStream);

            HttpSolet soletCandidate = null;

            String genericRequestPath = request
                    .getRequestUrl()
                    .substring(0,
                            request.getRequestUrl().indexOf("/", request.getRequestUrl().indexOf("/") + 1) + 1)
                    + "*";

            String requestPath = request.getRequestUrl();


            if(this.applicationLoader.getSolets().containsKey(genericRequestPath)){
                soletCandidate = this.applicationLoader.getSolets().get(genericRequestPath);
            }else if(this.applicationLoader.getSolets().containsKey(requestPath)){
                soletCandidate = this.applicationLoader.getSolets().get(requestPath);
            }

            if(!soletCandidate.isInitialized()){
                soletCandidate.init();
            }


            if(soletCandidate != null && soletCandidate.isInitialized()){
                soletCandidate.service(request, response);

                new Writer().writeBytes(response.getBytes(), outputStream);
                this.intercepted = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            this.intercepted = false;
        }
    }

    @Override
    public boolean hasIntercepted() {
        return this.intercepted;
    }
}
