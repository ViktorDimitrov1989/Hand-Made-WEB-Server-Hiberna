package org.softuni.broccolina;

import org.softuni.broccolina.solet.HttpSolet;
import org.softuni.broccolina.util.SoletLoader;
import org.softuni.javache.RequestHandler;
import org.softuni.javache.http.HttpResponse;
import org.softuni.javache.http.HttpResponseImpl;
import org.softuni.javache.http.HttpStatus;
import org.softuni.javache.io.Writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class SoletDispatcher implements RequestHandler{

    private final String SERVER_ROOT_PATH;

    private boolean intercepted;

    private SoletLoader soletLoader;

    public SoletDispatcher(String serverRootPath){
        this.intercepted = false;
        this.SERVER_ROOT_PATH = serverRootPath;
        this.soletLoader = new SoletLoader(this.SERVER_ROOT_PATH);
        this.soletLoader.loadSolets();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        StringBuilder responseContent = new StringBuilder();

        for (Map.Entry<String, HttpSolet> soletEntry : this.soletLoader.getLoadedSolets().entrySet()) {

            System.out.println(soletEntry.getKey() + "< - >" + soletEntry.getValue().getClass().getSimpleName());
            responseContent.append(soletEntry.getKey() + "< - >" + soletEntry.getValue().getClass().getSimpleName());
        }

        HttpResponse response = new HttpResponseImpl();

        response.setStatusCode(HttpStatus.OK);
        response.addHeader("Content-Type", "text/html");
        response.setContent(responseContent.toString().getBytes());

        try {
            Writer.writeBytes(response.getBytes(), outputStream);
            this.intercepted = true;
        } catch (IOException e) {
            this.intercepted = false;
        }

    }

    @Override
    public boolean hasIntercepted() {
        return this.intercepted;
    }
}
