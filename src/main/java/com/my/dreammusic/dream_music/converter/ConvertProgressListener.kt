package com.my.dreammusic.dream_music.converter

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import ws.schild.jave.info.MultimediaInfo
import ws.schild.jave.progress.EncoderProgressListener

class ConvertProgressListener : EncoderProgressListener {

    private val progressProperty = SimpleDoubleProperty()

    override fun sourceInfo(info: MultimediaInfo?) {
        // nothing
    }

    override fun progress(permil: Int) {
        progressProperty.set(permil / 1000.0)
    }

    override fun message(message: String?) {
        // nothing
    }

    fun getProgressProperty(): DoubleProperty {
        return progressProperty
    }
}