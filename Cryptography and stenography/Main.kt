package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor

fun main() {
    var input: String
    while(true){
        println("Task (hide, show, exit):")
        input = readln()
        when (input) {
            "hide" -> hideInterface()
            "show" -> showInterface()
            "exit" -> {
                println("Bye!")
                break
            }
            else -> println("Wrong task: $input")
        }
    }
}

fun hideInterface() {
    try{
        println("Input image file:")
        val inputFile = File(readln())
        println("Output image file:")
        val outputFile = File(readln())
        println("Message to hide:")
        val message: String = readln()
        println("Password:")
        val password: String = readln()
        ImageIO.write(hiding(inputFile, message, password), "png", 
outputFile)
        println("Message saved in $outputFile image.")
    }
    catch (e: Exception) {
        println("Can't read input file!")
    }
}

fun hiding(file: File, message: String, password: String): BufferedImage {
    val bufferFile = ImageIO.read(file)
    val msgBits: MutableList<Int> = encrypt(message, password).map { 
it.toString().toInt() } as MutableList<Int>
    val imgBits = mutableListOf<Int>()
    for (i in 0 until bufferFile.height) {
        for (j in 0 until bufferFile.width) {
            val pixCol = Color(bufferFile.getRGB(j,i))
            imgBits += pixCol.blue
        }
    }
    if (msgBits.size > imgBits.size) {
        println("The input image is not large enough to hold this 
message.")
        return bufferFile
    }

    for (i in msgBits.indices) {
        if (imgBits[i] % 2 == 0) {
            if (msgBits[i] == 1) imgBits[i] = imgBits[i] or 1
        }
        else {
            if (msgBits[i] == 0) imgBits[i] = (imgBits[i] xor 1)
        }
    }

    for (i in 0 until bufferFile.height) {
        for (j in 0 until bufferFile.width) {
            val color = Color(bufferFile.getRGB(j, i))
            val rgb = Color(color.red, color.green, imgBits[i * 
bufferFile.width + j]).rgb
            bufferFile.setRGB(j, i, rgb)
        }
    }
    return bufferFile
}

fun encrypt(message: String, password: String): String {
    val msgByte = message.encodeToByteArray()
    val pwdByte = password.encodeToByteArray()
    val msgEncryptedByte = encryptXor(msgByte, pwdByte)
    var msgEncryptedBits: String = ""
    for (i in msgEncryptedByte.indices) {
        msgEncryptedBits += 
(msgEncryptedByte[i].toInt().toString(2)).padStart(8, '0')
    }
    return "${msgEncryptedBits}000000000000000000000011"
}

fun encryptXor(message: ByteArray, password: ByteArray): ByteArray {
    val encryptedMessageByteArray = ByteArray(message.size)
    var counter = 0
    for (i in message.indices) {
        if (i > password.size - 1) {
            encryptedMessageByteArray[i] = message[i] xor 
password[counter]
            counter++
            if (counter > password.size - 1) {
                counter = 0
            }
        } else
            encryptedMessageByteArray[i] = message[i] xor password[i]
    }
    return encryptedMessageByteArray
}

fun showInterface() {
    try{
        println("Input image file:")
        val inputFile = File(readln().toString())
        println("Password:")
        val password: String = readln()
        println("Message:")
        showing(inputFile, password)
    } catch (e: Exception) {
        println("Error while showing message!")
    }
}

fun showing(file: File, password: String) {
    try {

        val image = ImageIO.read(file)
        var msgBits = ""
        loop@ for (i in 0 until image.height) {
            for (j in 0 until image.width) {
                val pixCol = Color(image.getRGB(j, i))
                msgBits += pixCol.blue % 2
                if ("000000000000000000000011" in msgBits && 
msgBits.length % 8 == 0) {
                    msgBits = msgBits.dropLast(24)
                    break@loop
                }
            }
        }
        val msgByte = ByteArray(msgBits.length / 8)
        for (i in msgBits.indices step 8) {
            val strByte = msgBits.slice(i..i + 7)
            val value = strByte.toInt(2)
            msgByte[i / 8] = value.toByte()
        }
        println(encryptXor(msgByte, 
password.encodeToByteArray()).toString(Charsets.UTF_8))
    } catch (e: Exception) {
        println("Error while showing message!")
    }
}
