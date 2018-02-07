package org.softuni.javache;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class RequestHandlerLoader {
    private static final String SERVER_FOLDER_PATH = "org/softuni/javache/";
    private static final String LIB_FOLDER_PATH = Server.class
            .getResource("")
            .getPath()
            .replace(SERVER_FOLDER_PATH, "lib");


    public Set<RequestHandler> loadRequestHandlers(){
        Set<RequestHandler> requestHandlers = new HashSet<>();

        this.loadFile(LIB_FOLDER_PATH, requestHandlers);

        return requestHandlers;
    }

    public void loadFile(String path, Set<RequestHandler> requestHandlers){

        File currentFileOrDirectory = new File(path);

        if(!currentFileOrDirectory.exists()){
            return;
        }

        for (File childFileOrDirectory : currentFileOrDirectory.listFiles()) {

            if(childFileOrDirectory.isDirectory()){
                this.loadFile(
                        path
                                + "/"
                                + childFileOrDirectory.getName(),
                        requestHandlers);
            }else{
                URLClassLoader urlClassLoader = null;
                try {
                    urlClassLoader = new URLClassLoader(
                        new URL[]{
                                new File(LIB_FOLDER_PATH)
                                        .toURI()
                                        .toURL()
                        });

                    Class<?> clazz = urlClassLoader
                            .loadClass(childFileOrDirectory.getName()
                                    .replace(".class", ""));

                    if(RequestHandler.class.isAssignableFrom(clazz)){
                        RequestHandler clazzInstance = (RequestHandler) clazz
                                .getConstructor(String.class)
                                .newInstance(WebConstants.WEB_SERVER_ROOT_FOLDER_PATH);

                        requestHandlers.add(clazzInstance);
                    }



                } catch (ReflectiveOperationException
                        | MalformedURLException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
