package org.softuni.broccolina;

import org.softuni.broccolina.solet.*;
import org.softuni.broccolina.util.SoletLoader;
import org.softuni.javache.RequestHandler;
import org.softuni.javache.http.*;
import org.softuni.javache.io.Reader;
import org.softuni.javache.io.Writer;

import java.io.*;
import java.util.Map;

public class SoletDispatcher implements RequestHandler {

    private final String SERVER_ROOT_PATH;

    private boolean intercepted;

    private SoletLoader soletLoader;

    public SoletDispatcher(String serverRootPath) {
        this.SERVER_ROOT_PATH = serverRootPath;
        this.intercepted = false;
        this.soletLoader = new SoletLoader(SERVER_ROOT_PATH);
        this.soletLoader.loadSolets();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            String requestContent = new Reader().readAllLines(inputStream);

            HttpSoletRequest request = new HttpSoletRequestImpl(requestContent, null);
            HttpSoletResponse response = new HttpSoletResponseImpl(outputStream);

            HttpSolet soletCandidate = null;

            for (Map.Entry<String, HttpSolet> soletEntry
                    : this.soletLoader.getSolets().entrySet()) {

                String soletRoute = soletEntry.getKey();

                if (soletRoute.equals(request.getRequestUrl())) {
                    soletCandidate = soletEntry.getValue();

                    if(!soletCandidate.isInitialized()){
                        soletCandidate.init();
                    }

                }
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
