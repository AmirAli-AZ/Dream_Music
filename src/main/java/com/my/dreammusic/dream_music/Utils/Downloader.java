package com.my.dreammusic.dream_music.Utils;

import javafx.concurrent.Task;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Downloader extends Task<Void> {

    public interface DownloaderListener {
        void onProgress(int progress);
        void onFailed();
        void onCompleted();
    }

    private URL fileURL;
    private File file, downloadPath;
    private DownloaderListener listener;
    private boolean exit = false;

    public Downloader(DownloaderListener listener) {
        this.listener = listener;
        this.downloadPath = new File(System.getProperty("user.home"));
    }

    public void setFileURL(String url) {
        try {
            URL resourceUrl = new URL(url);
            if (!("file".equals(resourceUrl.getProtocol()) && new File(resourceUrl.toURI()).isDirectory())) {
                fileURL = resourceUrl;
                String path = downloadPath.getAbsolutePath() + File.separator + getFileName();
                int i = 0;
                String name = path.substring(0 , path.lastIndexOf('.'));
                while (true){
                    if (new File(path).exists()){
                        i++;
                        path = name + i + "." + getExtension();
                    }else {
                        break;
                    }
                }
                file = new File(path);
            }
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String getExtension() {
        return fileURL != null ? FilenameUtils.getExtension(fileURL.getPath()) : "";
    }

    public String getFileBaseName() {
        return fileURL != null ? FilenameUtils.getBaseName(fileURL.getPath()) : "";
    }

    public String getFileName() {
        return fileURL != null ? FilenameUtils.getName(fileURL.getPath()) : "";
    }

    public void setDownloadPath(File path) {
        this.downloadPath = path;
    }

    @Override
    protected Void call() throws Exception {
        if (fileURL != null) {
            HttpURLConnection httpURLConnection = (HttpURLConnection) (fileURL.openConnection());
            long completeFileSize = httpURLConnection.getContentLength();

            try(BufferedInputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);) {
                byte[] data = new byte[1024];
                long downloadedFileSize = 0;
                int x;
                while ((x = in.read(data, 0, 1024)) >= 0 && !exit) {
                    downloadedFileSize += x;
                    final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100d);
                    listener.onProgress(currentProgress);
                    updateProgress(downloadedFileSize, completeFileSize);
                    bout.write(data, 0, x);
                }
                bout.close();
                in.close();
                if (exit && file.exists()) {
                    file.delete();
                }
            }
        }else {
            listener.onFailed();
        }

        return null;
    }

    @Override
    protected void succeeded() {
        if (!exit) listener.onCompleted();
    }

    @Override
    protected void failed() {
        listener.onFailed();
    }

    public void exit(){
        this.exit = true;
    }
}
