package me.pqpo.aipoet

import android.content.Context
import org.tensorflow.lite.Interpreter

import java.io.*
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class AiPoet @Throws(IOException::class)
@JvmOverloads constructor(context: Context, modelFile: String = MODEL_FILE, convertFile: String = CONVERT_FILE) {

    private val convert = Convert()

    private val input: ByteBuffer
    private val output: ByteBuffer
    private val states: Array<ByteBuffer>

    private val inputs = arrayOfNulls<ByteBuffer>(2)
    private val outputs = mutableMapOf<Int, Any>()

    private var inputStateIndex = 0

    private val tfLite: Interpreter

    init {
        convert.loadConvertFile(context.assets.openFd(convertFile))

        val tfLiteModel:MappedByteBuffer = loadModelFile(context, modelFile)
        val tfLiteOptions = Interpreter.Options()
        tfLite = Interpreter(tfLiteModel, tfLiteOptions)

        input = ByteBuffer.allocateDirect(Integer.SIZE / java.lang.Byte.SIZE)
        input.order(ByteOrder.nativeOrder())
        output = ByteBuffer.allocateDirect(Integer.SIZE / java.lang.Byte.SIZE)
        output.order(ByteOrder.nativeOrder())

        states = arrayOf(
            ByteBuffer.allocateDirect(HIDDEN_SIZE * Integer.SIZE / java.lang.Byte.SIZE),
            ByteBuffer.allocateDirect(HIDDEN_SIZE * Integer.SIZE / java.lang.Byte.SIZE)
        ).apply {
            this[0].order(ByteOrder.nativeOrder())
            this[1].order(ByteOrder.nativeOrder())
        }
    }

    fun reset() {
        input.clear()
        for (state in states) {
            state.clear()
            while (state.hasRemaining()) {
                state.putInt(0)
            }
            state.clear()
        }
        inputStateIndex = 0
        output.clear()
    }

    @Synchronized
    @Throws(UnmappedWordException::class)
    fun fetchNext(word: String): String {
        val wordIndex = convert.word2Index(word)
        if (wordIndex == -1) {
            throw UnmappedWordException(word)
        }

        val inputState = states[inputStateIndex]
        val outputState = states[1 - inputStateIndex]

        input.clear()
        input.putInt(convert.word2Index(word))
        inputState.rewind()
        output.clear()
        outputState.clear()
        inputs[0] = input
        inputs[1] = inputState
        outputs[0] = output
        outputs[1] = outputState
        tfLite.runForMultipleInputsOutputs(inputs, outputs)
        output.rewind()
        inputStateIndex = 1 - inputStateIndex
        val index = output.int
        return convert.index2Word(index)
    }

    @Throws(IOException::class)
    private fun loadModelFile(activity: Context, modelFile: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(UnmappedWordException::class)
    fun song(startWords:String, prefixWords:String, acrostic:Boolean=false, line:Int=5):String {
        val startWordsLocal = replacePunctuation(startWords)
        val prefixWordsLocal = replacePunctuation(prefixWords)
        reset()
        var preWord = "<START>"
        val sb = StringBuilder()
        for (i in 0 until prefixWordsLocal.length) {
            fetchNext(preWord)
            preWord = prefixWordsLocal[i].toString()
        }
        val lineSize = if (acrostic) startWordsLocal.length  else line
        var lineIndex = -1
        for (i in IntRange(0, MAX_CHAR_COUNT)) {
            val isNewLine = preWord in arrayOf("。", "！", "<START>", "？")
            if (isNewLine) {
                lineIndex++
            }
            if (lineIndex>=lineSize) {
                break
            }
            var newWord = fetchNext(preWord)
            if (newWord == "<EOP>") {
                break
            }
            if (acrostic) {
                if (isNewLine) {
                    newWord = startWordsLocal[lineIndex].toString()
                }
            } else if (i < startWordsLocal.length) {
                newWord = startWordsLocal[i].toString()
            }
            sb.append(newWord)
            preWord = newWord?:""
            if (newWord in arrayOf("。", "！", "？")) {
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    private fun replacePunctuation(words: String): String {
        return words.replace(",", "，")
            .replace(".", "。")
            .replace("?","？")
            .replace("!", "！")
    }

    companion object {

        private const val CONVERT_FILE = "convert.model"
        private const val MODEL_FILE = "poetry_gen_lite_model_quantize.tflite"
        private const val HIDDEN_SIZE = 1024
        private const val MAX_CHAR_COUNT = 200

    }

}
