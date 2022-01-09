package com.my.dreammusic.dream_music;

public interface Listener {
    int OK = 1 , CANCEL = 0;
    void onResult(int result);
}
