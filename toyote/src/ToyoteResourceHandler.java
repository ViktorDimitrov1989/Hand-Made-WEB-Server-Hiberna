import org.softuni.javache.RequestHandler;
import org.softuni.javache.http.*;
import org.softuni.javache.io.Reader;
import org.softuni.javache.io.Writer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ToyoteResourceHandler implements RequestHandler {

    private static final String STATIC_FOLDER = "static";

    private String serverRootPath;

    private boolean intercepted;

    public ToyoteResourceHandler(String serverRootPath){
        this.serverRootPath = serverRootPath;
        this.intercepted = false;
    }

    private void retrieveResource(HttpRequest request, HttpResponse response) throws IOException {
        String resourcesUrl = request.getRequestUrl();

        Path resourcePath = Paths.get(this.serverRootPath + STATIC_FOLDER + resourcesUrl);


        byte[] fileContentData = Files.readAllBytes(resourcePath);

        String fileContentType = Files.probeContentType(resourcePath);


        response.setContent(fileContentData);
        response.setStatusCode(HttpStatus.OK);
        response.addHeader("Content-Type", fileContentType);
        response.addHeader("Content-Length", fileContentData.length + "");
        response.addHeader("Content-Disposition", "inline");
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            HttpRequest request = new HttpRequestImpl(Reader.readAllLines(inputStream));
            HttpResponse response = new HttpResponseImpl();

            this.retrieveResource(request, response);

            Writer.writeBytes(response.getBytes(), outputStream);
            this.intercepted = true;
            System.out.println(response.getStatusCode());
        } catch (IOException e) {
            this.intercepted = false;
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }


    @Override
    public boolean hasIntercepted() {
        return this.intercepted;
    }
}
