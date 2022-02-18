package com.my.dreammusic.dream_music.logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class Logger {

    private final Class<?> clazz;
    private BufferedWriter bufferedWriter = null;
    private boolean appendConsole;
    private String dateFormat = "dd MMM hh:mm:ss";
    private final PrintStream console = System.out;
    private final PrintStream errorConsole = System.err;

    private static Logger logger;

    private Logger(Class<?> clazz) {
        this.clazz = clazz;
        this.appendConsole = true;
    }

    private Logger(Class<?> clazz , boolean appendConsole) {
        this.clazz = clazz;
        this.appendConsole = appendConsole;
    }

    public static Logger getLogger(Class<?> clazz){
        return logger = new Logger(clazz);
    }

    public static Logger getLogger(Class<?> clazz , boolean appendConsole){
        return logger = new Logger(clazz , appendConsole);
    }

    public void info(String msg){
        log(Level.INFO , msg);
    }

    public void warn(String msg) {
        log(Level.WARNING , msg);
    }

    public void error(Throwable e){
        log(Level.ERROR , e.getMessage() , e);
    }

    public void error(String msg , Throwable e){
        log(Level.ERROR , msg , e);
    }

    public void log(Level level , String msg) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        String layout = "[" + level + "] " +
                "[" + clazz.getName() + "] " +
                "[" + simpleDateFormat.format(Calendar.getInstance().getTime()) + "] " +
                msg;
        if (appendConsole) {
            if (level == Level.ERROR) errorConsole.println(layout);
            else console.println(layout);
        }
        try {
            write(layout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Level level , String msg , Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        String layout = "[" + level + "] " +
                "[" + clazz.getName() + "] " +
                "[" + simpleDateFormat.format(Calendar.getInstance().getTime()) + "] " +
                msg + " : \n" + sw;
        if (appendConsole) {
            if (level == Level.ERROR) errorConsole.println(layout);
            else console.println(layout);
        }
        try {
            write(layout);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isAppendConsole() {
        return appendConsole;
    }

    public void setAppendConsole(boolean appendConsole) {
        this.appendConsole = appendConsole;
    }

    public void setDateFormat(String dateFormat) {
        try {
            new SimpleDateFormat(dateFormat);
        }catch (Exception e){
            return;
        }
        this.dateFormat = dateFormat;
    }

    public void setWriter(String path , boolean append) throws IOException {
        File logFile = new File(path);
        if (!isValidPath(path) || logFile.isDirectory()) return;
        createDirs(logFile.getParentFile());
        bufferedWriter = new BufferedWriter(new FileWriter(path , append));
    }

    private void write(String s) throws IOException {
        if (bufferedWriter != null) {
            bufferedWriter.write(s + "\n");
            bufferedWriter.flush();
        }
    }

    private boolean isValidPath(String path) {
        if (path.length() == 0) return false;
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private void createDirs(File parentFile) throws IOException {
        if (!parentFile.exists()) {
            Files.createDirectories(Paths.get(parentFile.getAbsolutePath()));
            if (!parentFile.getParentFile().exists())
                createDirs(parentFile.getParentFile());
        }
    }

    public String getAbsoluteName() {
        return clazz.getName();
    }

    public String getName() {
        return clazz.getSimpleName();
    }
}
