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
        for (File directory : translateIntoDirectories(pkgname)) {
            if (directory != null && directory.exists()) {
                String[] files = directory.list();
                if (files != null) {
                    for (String file : files) {
                        if ((file.endsWith(".class") || file.endsWith(".java")) && !file.contains("$")) {
                            String className = pkgname + '.' + file.replaceAll("\\.(class|java)$","");
                            if (!className.endsWith("Test") && !classes.contains(className)) {
                                classes.add(className);
                            }
                        }
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
        for (File directory : translateIntoDirectories(pkgname)) {
            if (directory != null && directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                if (files != null) {
                    for (String file : files) {
                        if (file.contains(".")) {
                            continue;
                        }
                        String subpkgname = pkgname + '.' + file;
                        File subDir = new File(directory, file);
                        if (subDir.isDirectory() && !subpkgs.contains(subpkgname)) {
                            subpkgs.add(subpkgname);
                        }
                    }
                }
            }
        }
        return subpkgs;
    }

    private static ArrayList<File> translateIntoDirectories(String pkgname){
        ArrayList<File> dirs = new ArrayList<>();
        String relPath = pkgname.replace('.', '/');
        
        try {
            java.util.Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(relPath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try {
                    dirs.add(new File(resource.toURI()));
                } catch (Exception e) {
                    dirs.add(new File(resource.getPath()));
                }
            }
        } catch (Exception e) {
            // ignore
        }
        
        if (dirs.isEmpty()) {
            try {
                java.util.Enumeration<URL> resources = Reflections.class.getClassLoader().getResources(relPath);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    try {
                        dirs.add(new File(resource.toURI()));
                    } catch (Exception e) {
                        dirs.add(new File(resource.getPath()));
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        
        if (dirs.isEmpty()) {
            try {
                java.util.Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources(relPath);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    try {
                        dirs.add(new File(resource.toURI()));
                    } catch (Exception e) {
                        dirs.add(new File(resource.getPath()));
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        
        File srcDir = new File("src/main/java/" + relPath);
        if (srcDir.exists() && !dirs.contains(srcDir)) {
            dirs.add(srcDir);
        }
        File testDir = new File("src/test/java/" + relPath);
        if (testDir.exists() && !dirs.contains(testDir)) {
            dirs.add(testDir);
        }
        return dirs;
    }
}
