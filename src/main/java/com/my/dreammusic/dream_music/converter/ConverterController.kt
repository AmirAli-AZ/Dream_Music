package com.my.dreammusic.dream_music.converter

import com.jthemedetecor.OsThemeDetector
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import ws.schild.jave.Encoder
import ws.schild.jave.MultimediaObject
import ws.schild.jave.encode.AudioAttributes
import ws.schild.jave.encode.EncodingAttributes
import java.io.File
import java.net.URL
import java.util.*
import java.util.function.Consumer
import com.my.dreammusic.dream_music.Dialog
import com.my.dreammusic.dream_music.Listener

class ConverterController : Initializable {

    @FXML
    private lateinit var convertBtn: Button

    @FXML
    private lateinit var progress: ProgressBar

    @FXML
    private lateinit var bitRateSlider: Slider

    @FXML
    private lateinit var root: BorderPane

    @FXML
    private lateinit var sampleRates: ComboBox<Double>

    @FXML
    private lateinit var sourcePathText: Label

    @FXML
    private lateinit var targetPathText: Label

    @FXML
    private lateinit var toolTip: Tooltip

    @FXML
    private lateinit var imgMovie: ImageView

    @FXML
    private lateinit var imgMusic: ImageView


    private var source:File? = null
    private var target:File? = null

    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {
        convertBtn.isDisable = true
        sampleRates.items.addAll(
            8.0,11.025,12.0,16.0,22.05,24.0,32.0,44.1,48.0
        )
        sampleRates.selectionModel.select(44.1)
        bitRateSlider.valueProperty().addListener { _, _, newValue ->
            toolTip.text = newValue.toInt().toString()
        }
        Platform.runLater {
            if (OsThemeDetector.isSupported()){
                val light = ConverterController::class.java.getResource("/com/my/dreammusic/dream_music/Themes/dialog-light-theme.css")!!.toExternalForm()
                val dark = ConverterController::class.java.getResource("/com/my/dreammusic/dream_music/Themes/dialog-dark-theme.css")!!.toExternalForm()
                val scene = root.scene
                val detector  = OsThemeDetector.getDetector()
                val darkThemeListener:Consumer<Boolean> = Consumer {
                    if (it) {
                        scene.stylesheets[0] = dark
                        imgMovie.image = Image(ConverterController::class.java.getResourceAsStream("/com/my/dreammusic/dream_music/icons/baseline_movie_white_24dp.png"))
                        imgMusic.image = Image(ConverterController::class.java.getResourceAsStream("/com/my/dreammusic/dream_music/icons/ic_music_white.png"))
                    }
                    else {
                        scene.stylesheets[0] = light
                        imgMovie.image = Image(ConverterController::class.java.getResourceAsStream("/com/my/dreammusic/dream_music/icons/baseline_movie_black_24dp.png"))
                        imgMusic.image = Image(ConverterController::class.java.getResourceAsStream("/com/my/dreammusic/dream_music/icons/ic_music_black.png"))
                    }
                }
                darkThemeListener.accept(detector.isDark)
                detector.registerListener(darkThemeListener)
            }
        }
    }

    @FXML
    fun pickSource(event: MouseEvent) {
        if (event.button != MouseButton.PRIMARY) return
        pickVideo()
    }

    @FXML
    fun pickTarget(event: MouseEvent) {
        if (event.button != MouseButton.PRIMARY) return
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.title = "save file"
        fileChooser.extensionFilters.add(ExtensionFilter("MP3 FILE (.mp3)" , "*.mp3"))
        var file = fileChooser.showSaveDialog(root.scene.window)

        if (file != null) {
            file = fixFormat(file)
            if (source != null) convertBtn.isDisable = false
            target = file
            targetPathText.text = file.absolutePath
        }
    }

    @FXML
    fun convert() {
        convertBtn.isDisable = true
        if (source == null || source?.exists() == false){
            sourcePathText.text = ""
            val dialog = Dialog(root.scene.window)
            dialog.setListener {
                if (it == Listener.OK){
                    val file = pickVideo()
                    if (file != null) {
                        dialog.close()
                        sourcePathText.text = file.absolutePath
                    }
                }
            }
            dialog.setImage(Image(ConverterController::class.java.getResourceAsStream("/com/my/dreammusic/dream_music/icons/ic_warning.png")))
            dialog.setTitle("source not exist")
            dialog.setMessage("Source not exist , please pick source again.")
            dialog.setAutoClose(false)
            dialog.show()
            return
        }
        val task = object : Task<Void>() {
            override fun call(): Void? {
                val listener = ConvertProgressListener()
                val audioAttrs = AudioAttributes()
                audioAttrs.setCodec("libmp3lame")
                audioAttrs.setBitRate(bitRateSlider.value.toInt() * 1000)
                audioAttrs.setChannels(2)
                audioAttrs.setSamplingRate((sampleRates.selectionModel.selectedItem * 1000).toInt())

                val encodingAttributes = EncodingAttributes()
                encodingAttributes.setAudioAttributes(audioAttrs)

                this@ConverterController.progress.isVisible = true
                this@ConverterController.progress.progressProperty().bind(listener.getProgressProperty())

                val encoder = Encoder()
                encoder.encode(MultimediaObject(source) , target , encodingAttributes , listener)

                return null
            }

            override fun failed() {
                super.failed()
                this@ConverterController.progress.isVisible = false
                convertBtn.isDisable = false
            }

            override fun succeeded() {
                super.succeeded()
                this@ConverterController.progress.isVisible = false
                convertBtn.isDisable = false
            }
        }
        val thread = Thread(task)
        thread.isDaemon = true
        thread.start()
    }

    private fun pickVideo():File? {
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.title = "choose source"
        fileChooser.extensionFilters.add(ExtensionFilter("MP4 FILE (.mp4)" , "*.mp4"))
        val selectedFile = fileChooser.showOpenDialog(root.scene.window)

        if (selectedFile != null) {
            if (target != null) convertBtn.isDisable = false
            source = selectedFile
            sourcePathText.text = selectedFile.absolutePath
        }

        return selectedFile
    }

    private fun fixFormat(path:File):File {
        val index = path.name.indexOf('.')
        return if (index == -1)
            File(path.parent , "${path.name}.mp3")
        else
            File(path.parent , "${path.name.substring(0 , index)}.mp3")
    }
}