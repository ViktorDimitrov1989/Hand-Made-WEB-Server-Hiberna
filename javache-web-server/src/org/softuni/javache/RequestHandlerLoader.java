package org.softuni.javache;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RequestHandlerLoader {
    private static final String SERVER_FOLDER_PATH = "org/softuni/javache/";
    private static final String LIB_FOLDER_PATH = Server.class
            .getResource("")
            .getPath()
            .replace(SERVER_FOLDER_PATH, "lib");

    private ServerConfig serverConfig;

    public RequestHandlerLoader(ServerConfig serverConfig){
        this.serverConfig = serverConfig;
    }

    public LinkedList<RequestHandler> loadRequestHandlers(){
        LinkedList<RequestHandler> requestHandlers = new LinkedList<>();

        this.loadFile(LIB_FOLDER_PATH, requestHandlers);

        return requestHandlers;
    }

    public void loadFile(String path, LinkedList<RequestHandler> requestHandlers){

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

                    String clazzName = childFileOrDirectory.getName()
                            .replace(".class", "");

                    Class<?> clazz = urlClassLoader
                            .loadClass(clazzName);

                    if(RequestHandler.class.isAssignableFrom(clazz)){
                        RequestHandler clazzInstance = (RequestHandler) clazz
                                .getConstructor(String.class)
                                .newInstance(WebConstants.WEB_SERVER_ROOT_FOLDER_PATH);

                        int orderIndex = this.serverConfig.getHandlerIndexByName(clazzName);

                        requestHandlers.add(orderIndex, clazzInstance);
                    }



                } catch (ReflectiveOperationException
                        | MalformedURLException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
