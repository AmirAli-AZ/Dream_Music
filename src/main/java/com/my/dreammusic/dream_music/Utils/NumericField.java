package com.my.dreammusic.dream_music.Utils;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class NumericField extends TextField {

    public NumericField() {
        TextFormatter<String> formatter = new TextFormatter<String>(change -> {
            change.setText(change.getText().replaceAll("[^0-9]" , ""));
            return change;
        });
        setTextFormatter(formatter);
    }

    public int getValue(){
        String s = getText();
        if (s.length() != 0 || s != null) return Integer.parseInt(s);
        else return 0;
    }

    public void setValue(int i){
        setText(String.valueOf(i));
    }
}