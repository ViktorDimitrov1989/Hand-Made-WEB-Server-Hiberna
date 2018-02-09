package org.softuni.broccolina.util;

import org.softuni.broccolina.solet.HttpSolet;
import org.softuni.javache.RequestHandler;
import org.softuni.javache.WebConstants;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SoletLoader {

    public static String APPLICATION_FOLDER_PATH;

    private Map<String, HttpSolet> loadedSoletsByApplicationName;

    public SoletLoader(String serverRootPath){
        this.APPLICATION_FOLDER_PATH =
                serverRootPath + "apps";

        this.loadedSoletsByApplicationName = new HashMap<>();
    }


    private void loadLibraries(String libFolderPath) throws IOException {

        File libDirectory = new File(libFolderPath);

        if(libDirectory.exists() && libDirectory.isDirectory()){

            for (File file : libDirectory.listFiles()) {
                if(!this.isLibraryFile(file)){
                    continue;
                }

                JarFile library = new JarFile(file.getCanonicalPath());

                this.loadLibrary(library, file.getCanonicalPath());

            }
        }
    }

    private void loadLibrary(JarFile library, String canonicalPath) {
        Enumeration<JarEntry> fileEntries = library.entries();

        try {
            URL[] urls = { new URL("jar:file:" + canonicalPath + "!/") };
            URLClassLoader ucl = URLClassLoader.newInstance(urls);

            while (fileEntries.hasMoreElements()){
                JarEntry currentFile = fileEntries.nextElement();

                if(currentFile.isDirectory()
                        || !currentFile.getName().endsWith(".class")){
                    continue;
                }

                String className = currentFile.getName()
                        .replace(".class", "")
                        .replace("/", ".");

                System.out.println(className);

                Class soletClazz = ucl.loadClass(className);

                if(HttpSolet.class.isAssignableFrom(soletClazz)){

                    HttpSolet soletObj = (HttpSolet) soletClazz.getConstructor()
                            .newInstance();

                    String applicationPath = new File(canonicalPath).getParent();

                    this.loadedSoletsByApplicationName
                            .putIfAbsent(applicationPath
                                            .substring(applicationPath
                                                    .lastIndexOf("/") + 1),
                                    soletObj);
                }

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException
                | InvocationTargetException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private boolean isLibraryFile(File file) {
        return file.getName().endsWith(".jar");

    }

    public Map<String, HttpSolet> getLoadedSolets(){
        return Collections.unmodifiableMap(this.loadedSoletsByApplicationName);
    }

    public void loadSolets(){
        this.loadedSoletsByApplicationName = new HashMap<>();

        try {
            File appsDir = new File(this.APPLICATION_FOLDER_PATH);

            if(!appsDir.exists()){
                return;
            }

            for (File file : appsDir.listFiles()) {
                this.loadLibraries(file.getCanonicalPath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }








}
