package org.softuni.javache.util;

import org.softuni.javache.WebConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ServerConfig {

    private static final String INIT_FOLDER_NAME = "init";
    private static final String CONFIG_FILE_NAME = "config.ini";
    private static final String REQUEST_HANDLERS_INIT_KEY = "request-handlers";

    private LinkedList<String> handlersOrderConfiguration;

    public ServerConfig(){
        this.initConfiguration();
    }

    private void initConfiguration() {
        String configFilePath = WebConstants.WEB_SERVER_ROOT_FOLDER_PATH + INIT_FOLDER_NAME + File.separator + CONFIG_FILE_NAME;

        try {
            List<String> lines = Files.readAllLines(Paths.get(configFilePath));

            for (String line : lines) {
                if(line.startsWith(REQUEST_HANDLERS_INIT_KEY)){
                    this.handlersOrderConfiguration = new LinkedList<>(Arrays.asList(line.split("\\:")[1].trim().split(",\\s")));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getHandlerIndexByName(String handlerName){
        return this.handlersOrderConfiguration.indexOf(handlerName);
    }

}
