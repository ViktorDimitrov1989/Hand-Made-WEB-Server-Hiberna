import org.softuni.javache.RequestHandler;
import org.softuni.javache.http.*;
import org.softuni.javache.io.Reader;
import org.softuni.javache.io.Writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BroccoRequestHandler implements RequestHandler{

    private boolean intercepted;
    private final String serverRootPath;

    public BroccoRequestHandler(String serverRootPath){
        this.intercepted = false;
        this.serverRootPath = serverRootPath;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {

        try {

            String requestContent = Reader.readAllLines(inputStream);

            HttpRequest request = new HttpRequestImpl(requestContent);

            if(request.getRequestUrl().equals("/")){
                HttpResponse response = new HttpResponseImpl();

                response.setStatusCode(HttpStatus.OK);

                response.addHeader("Content-Type", "text/html");

                response.setContent(("<h1>Hello world!</h1><h2>" + this.serverRootPath + "</h2>").getBytes());

                Writer.writeBytes(response.getBytes(), outputStream);

                this.intercepted = true;
            }


        } catch (IOException e) {
            this.intercepted = false;
        }


    }

    @Override
    public boolean hasIntercepted() {
        return this.intercepted;
    }
}
