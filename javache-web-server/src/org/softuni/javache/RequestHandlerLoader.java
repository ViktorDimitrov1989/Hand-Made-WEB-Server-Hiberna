package org.softuni.javache;

import org.softuni.javache.util.ServerConfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RequestHandlerLoader {

    private static final String LIB_FOLDER_PATH = WebConstants.WEB_SERVER_ROOT_FOLDER_PATH + "lib";

    private Map<Integer, RequestHandler> loadedRequestHandlers;

    private void loadLibraries(String libFolderPath) throws IOException {

        File libDirectory = new File(LIB_FOLDER_PATH);

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
        ServerConfig serverConfig = new ServerConfig();

        try {
            URL[] urls = { new URL("jar:file:" + canonicalPath + "!/") };
            URLClassLoader ucl = URLClassLoader.newInstance(urls);

            while (fileEntries.hasMoreElements()){
                JarEntry currentFile = fileEntries.nextElement();

                if(currentFile.isDirectory()
                        || !currentFile.getName().endsWith(".class")){
                    continue;
                }

                //out/production/javache-web-server/lib/ToyoteResourceHandler.class
                String className = currentFile.getName()
                        .replace(".class", "")
                        .replace("/", ".");

                Class handlerClass = ucl.loadClass(className);

                if(RequestHandler.class.isAssignableFrom(handlerClass)){

                    RequestHandler handlerObj = (RequestHandler) handlerClass.getConstructor(String.class)
                            .newInstance(WebConstants.WEB_SERVER_ROOT_FOLDER_PATH);

                    this.loadedRequestHandlers
                            .putIfAbsent(serverConfig
                                    .getHandlerIndexByName(handlerObj
                                            .getClass()
                                            .getSimpleName()),
                                    handlerObj);
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

    public Map<Integer, RequestHandler> getLoadedRequestHandlers(){
        return Collections.unmodifiableMap(this.loadedRequestHandlers);
    }

    public void loadRequestHandlers(){
        this.loadedRequestHandlers = new TreeMap<>();

        try {
            this.loadLibraries(LIB_FOLDER_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
