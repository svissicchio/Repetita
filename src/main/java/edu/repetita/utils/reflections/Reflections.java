package edu.repetita.utils.reflections;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class Reflections {

    public static ArrayList<String> getClassesForPackage(Package pkg) {
        return getClassesForPackage(pkg.getName());
    }

    public static ArrayList<String> getClassesForPackage(String pkgname) {
        ArrayList<String> classes = new ArrayList<>();

        // remove additional information that might be added while packaging the source code as an executable
        pkgname = pkgname.split(",",2)[0];

        // Get the list of classes contained in the package
        File directory = translateIntoDirectory(pkgname);
        if (directory != null && directory.exists()) {
            String[] files = directory.list();
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".class") || file.endsWith(".java")) {
                        String className = pkgname + '.' + file.replaceAll(".(class|java)","");
                        classes.add(className);
                    }
                }
            }
        }

        return classes;
    }

    public static ArrayList<String> getPackagesInPackage(Package pkg) {
        return getPackagesInPackage(pkg.getName());
    }

    public static ArrayList<String> getPackagesInPackage(String pkgname) {
        ArrayList<String> subpkgs = new ArrayList<>();
        File directory = translateIntoDirectory(pkgname);

        if (directory != null && directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            if (files != null) {
                for (String file : files) {
                    if (file.contains(".")) {
                        continue;
                    }
                    String subpkgname = pkgname + '.' + file;
                    String uriString = getURIString(subpkgname.replaceAll("\\.", File.separator));
                    if (uriString != null) {
                        if (new File(uriString).isDirectory()) {
                            subpkgs.add(subpkgname);
                        }
                    }
                }
            }
        }
        return subpkgs;
    }

    // Helper methods

    private static File translateIntoDirectory(String pkgname){
        // Get a File object for the package
        File directory = null;
        String relPath = pkgname.replace('.', '/');

        String uriString = getURIString(relPath);
        if (uriString != null) {
            directory = new File(uriString);
        }

        return directory;
    }

    private static String getURIString(String relPath){
        URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);
        if (resource == null) {
            throw new RuntimeException("No resource for " + relPath);
        }

        URI uri = null;
        try {
            uri = resource.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error in URI syntax: " + e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Illegal argument during URI extraction: " + e);
        }

        // Translate URI in directory
        String uriString = uri.toString();
        uriString = uriString.replaceAll("file:","");

        // Remove potential substrings due to packaging into a jar
        uriString = uriString.replaceAll("jar:","");
        uriString = uriString.replaceAll("target.*!","src/main/java");

        return uriString;
    }
}
