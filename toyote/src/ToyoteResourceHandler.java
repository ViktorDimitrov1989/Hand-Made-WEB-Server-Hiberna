import org.softuni.javache.RequestHandler;
import org.softuni.javache.http.*;
import org.softuni.javache.io.Reader;
import org.softuni.javache.io.Writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ToyoteResourceHandler implements RequestHandler {

    private static final String STATIC_FOLDER = "static";

    private String serverRootPath;

    private boolean intercepted;

    public ToyoteResourceHandler(String serverRootPath){
        this.serverRootPath = serverRootPath;
        this.intercepted = false;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            HttpRequest request = new HttpRequestImpl(Reader.readAllLines(inputStream));
            HttpResponse response = new HttpResponseImpl();

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try {


            String resourceExtension = resourceUrl.substring(resourceUrl.lastIndexOf(".") + 1);

            byte[] content =
                    Files.readAllBytes(Paths.get(
                            staticFolder
                                    + resourceUrl));

            response.setStatusCode(HttpStatus.OK);

            response.addHeader("Content-Type", this.getContentType(resourceExtension));
            response.addHeader("Content-Length", content.length + "");
            response.addHeader("Content-Disposition", "inline");


            Writer.writeBytes(response.getBytes(), outputStream);
            this.intercepted = true;
        } catch (IOException e) {
            this.intercepted = false;
        }*/



    }

    @Override
    public boolean hasIntercepted() {
        return false;
    }

    private String getContentType(String resourceExtension) {
        switch(resourceExtension) {
            case "html": return "text/html";
            case "css": return "text/css";
            case "png": return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default: return "text/plain";
        }
    }
}
