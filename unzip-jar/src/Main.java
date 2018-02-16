import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String jarCanonicalPath = Main.class.getResource("").getPath() + "casebook.jar";

        File file = new File(jarCanonicalPath);

        System.out.println(file.getName());
    }

}
