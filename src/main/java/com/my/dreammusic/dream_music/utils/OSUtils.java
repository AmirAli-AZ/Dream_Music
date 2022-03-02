package com.my.dreammusic.dream_music.utils;

import java.io.IOException;
import java.net.URI;

public final class OSUtils {

    private OSUtils(){}

    private static String getOSName(){
        return System.getProperty("os.name").toLowerCase();
    }

    public static boolean isWindows(){
        return getOSName().contains("win");
    }

    public static boolean isLinux() {
        return getOSName().contains("nix") || getOSName().contains("nux");
    }

    public static void browse(URI link) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        if (isWindows()) {
            runtime.exec("explorer \"" + link.toString() + "\"");
        }else if (isLinux()) {
            runtime.exec("xdg-open " + link.toString());
        }
    }
}
