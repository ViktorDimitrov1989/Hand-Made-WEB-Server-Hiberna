package org.softuni.broccolina.util;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUnzipUtil {

    public JarUnzipUtil(){}

    public void unzipJar(String jarCanonicalPath) throws IOException {

        JarFile jar = new JarFile(new File(jarCanonicalPath));
        File outDirectory = new File(jarCanonicalPath.replace(".jar", ""));

        if(outDirectory.exists()){
            outDirectory.delete();
        }
        outDirectory.mkdir();

        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()){
            JarEntry currentEntry = entries.nextElement();

            File currentFile = new File(
                    outDirectory.getCanonicalPath()
                    + File.separator + currentEntry.getName());

            if(currentEntry.isDirectory()){
                currentFile.mkdir();
                continue;
            }

            InputStream inputStream = jar.getInputStream(currentEntry);
            OutputStream outputStream = new FileOutputStream(currentFile);


            while (inputStream.available() > 0){
                outputStream.write(inputStream.read());
            }

            outputStream.close();
            inputStream.close();
        }

        jar.close();
    }

}
