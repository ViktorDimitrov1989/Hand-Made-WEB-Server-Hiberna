package org.softuni.broccolina.util;

import org.softuni.broccolina.solet.HttpSolet;
import org.softuni.broccolina.solet.WebSolet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ApplicationLoader {
    private static final String ROOT_APPLICATION_FOLDER_NAME = "ROOT_APPLICATION_FOLDER_NAME";

    public final String APPLICATION_FOLDER_PATH;

    private JarUnzipUtil jarUnzipUtil;

    private HashMap<String, HttpSolet> loadedSoletsByApplicationName;

    public ApplicationLoader(String serverRootPath) {
        this.APPLICATION_FOLDER_PATH =
                serverRootPath + "apps";
        this.jarUnzipUtil = new JarUnzipUtil();
    }

    private void loadIfSolet(Class clazzFile, String applicationName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class parentClass = clazzFile.getSuperclass();


        if (parentClass != null &&
                Arrays.stream(parentClass.getSuperclass()
                .getInterfaces())
                .anyMatch(x -> x
                        .getSimpleName()
                        .equals(HttpSolet.class.getSimpleName()))
                && !Modifier.isAbstract(clazzFile.getModifiers())) {

            HttpSolet soletObject =
                    (HttpSolet) clazzFile.getConstructor()
                            .newInstance();

            WebSolet annotation = (WebSolet) clazzFile.getAnnotation(WebSolet.class);

            if(annotation.loadedOnStartUp()){
                soletObject.init();
            }


            applicationName = applicationName.equals(ROOT_APPLICATION_FOLDER_NAME)
                    ? ""
                    : "/" + applicationName;

            this.loadedSoletsByApplicationName
                    .putIfAbsent(applicationName
                                    + annotation.route()
                            , soletObject);
        }

    }

    private boolean isJarFile(File file) {
        return file.getName().endsWith(".jar");
    }

    private void unzipJar(File jarFile) throws IOException {
        this.jarUnzipUtil.unzipJar(jarFile.getCanonicalPath());
    }

    private void loadLibrary(JarFile library, String canonicalPath, String applicationName) {
        Enumeration<JarEntry> fileEntries = library.entries();

        try {
            URL[] urls = {new URL("jar:file:" + canonicalPath + "!/")};
            URLClassLoader ucl = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(ucl);

            while (fileEntries.hasMoreElements()) {
                JarEntry currentFile = fileEntries.nextElement();

                if (currentFile.isDirectory()
                        || !currentFile.getName().endsWith(".class")) continue;

                String className = currentFile.getName()
                        .replace(".class", "")
                        .replace("/", ".");

                Class soletClazz = ucl.loadClass(className);

                this.loadIfSolet(soletClazz, applicationName);
            }
        } catch (MalformedURLException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void loadClasses(String classFolderPath, String applicationName) {
        File classDirectory = new File(classFolderPath);

        if(!classDirectory.exists()){
            return;
        }

        this.loadClass(classDirectory, applicationName);
    }

    private void loadClass(File classFile, String applicationName) {
        if(classFile.isDirectory()){
            for (File childFile : classFile.listFiles()) {
                this.loadClass(childFile, applicationName);
            }
        }else{
            if(!classFile.getName().endsWith(".class")){
                return;
            }

            String parentPath = classFile.getParent();

            try {

                URL[] urls = new URL[]{new File(parentPath).toURI().toURL()};
                URLClassLoader ucl = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
                Thread.currentThread().setContextClassLoader(ucl);

                String className = classFile.getName()
                        .replace(".class", "")
                        .replace("/", ".");

                Class clazzFile = ucl.loadClass(className);

                this.loadIfSolet(clazzFile, applicationName);

            } catch (MalformedURLException
                    | ClassNotFoundException
                    | NoSuchMethodException
                    | IllegalAccessException
                    | InstantiationException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }


        }

    }

    private void loadLibraries(String libFolderPath, String applicationName) throws IOException {
        File libDirectory = new File(libFolderPath);

        if (libDirectory.exists() && libDirectory.isDirectory()) {
            for (File file : libDirectory.listFiles()) {
                if (!this.isJarFile(file)) {
                    continue;
                }

                JarFile library = new JarFile(file.getCanonicalPath());
                this.loadLibrary(library, file.getCanonicalPath(), applicationName);
            }
        }
    }

    public Map<String, HttpSolet> getSolets() {
        return Collections.unmodifiableMap(this.loadedSoletsByApplicationName);
    }

    public void loadApplications() {
        this.loadedSoletsByApplicationName = new HashMap<>();

        try {
            File appsDir = new File(APPLICATION_FOLDER_PATH);

            if (!appsDir.exists()) return;


            for (File file : appsDir.listFiles()) {
                if(this.isJarFile(file)){
                    this.unzipJar(file);

                    String applicationName = file.getName().replace(".jar", "");

                    String libraryPath = file.getCanonicalPath().replace(".jar", "") + File.separator + "lib";
                    String classesPath = file.getCanonicalPath().replace(".jar", "") + File.separator + "classes";

                    this.loadLibraries(libraryPath, applicationName);
                    this.loadClasses(classesPath, applicationName);

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
