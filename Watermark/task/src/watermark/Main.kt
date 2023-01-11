package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.imageio.ImageIO
import kotlin.system.exitProcess

val IMAGE_EXTENSION_REGEXP = ".*\\.(jpg|png)".toRegex()
const val EXTENSION_LENGTH = 3

enum class PositionMethods() {
    SINGLE,
    GRID
}

fun main() {
    println("Input the image filename:")
    val filename = readln()
    validateImageFile(filename)
    val image = ImageIO.read(File(filename))
    println("Input the watermark image filename:")
    val wmFilename = readln()
    validateImageFile(wmFilename, isWatermark = true)
    validateDimensions(filename, wmFilename)
    val watermark = ImageIO.read(File(wmFilename))
    var useAlphaChannel = false
    var transparencyColor: Color? = null
    if (hasTranslucentTransparency(watermark) == true) {
        println("Do you want to use the watermark's Alpha channel?")
        useAlphaChannel = (readln().lowercase() == "yes")
    } else {
        println("Do you want to set a transparency color?")
        val setTransparencyColor = (readln().lowercase() == "yes")
        if (setTransparencyColor) {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            try {
                val inputData = readln()
                val (r, g, b) = validateRGBInput(inputData)
                transparencyColor = Color(r!!, g!!, b!!)
            } catch (e: Exception) {
                println("The transparency color input is invalid.")
                exitProcess(0)
            }
        }
    }
    println("Input the watermark transparency percentage (Integer 0-100):")
    val transpPercentage = readln().toIntOrNull()
    validateTranspValue(transpPercentage)
    println("Choose the position method (single, grid):")
    val positionMethod = readln().uppercase()
    val position = validatePositionMethod(positionMethod, image, watermark)
    println("Input the output image filename (jpg or png extension):")
    val outputFilename = readln()
    validateFilename(outputFilename)
    val outputImage = blendImageAndWatermark(
        image,
        watermark,
        transpPercentage!!,
        useAlphaChannel,
        transparencyColor,
        PositionMethods.valueOf(positionMethod),
        position
    )
    val outputFile = File(outputFilename)
    ImageIO.write(outputImage, outputFilename.substring(outputFilename.length - EXTENSION_LENGTH), outputFile)
    println("The watermarked image $outputFilename has been created.")
}

fun validatePositionMethod(positionMethod: String, image: BufferedImage, watermark: BufferedImage): List<Int?>? {
    var position: List<Int?>? = null
    try {
        if (PositionMethods.valueOf(positionMethod) == PositionMethods.SINGLE) {
            val diffSize = mutableListOf(image.width - watermark.width, image.height - watermark.height)
            println("Input the watermark position ([x 0-${diffSize[0]}] [y 0-${diffSize[1]}]):")
            position = validatePositionInput(readln(), diffSize)
        }
    } catch (e: IllegalArgumentException) {
        println("The position method input is invalid.")
        exitProcess(0)
    }
    return position
}

fun validatePositionInput(input: String, diffSize: List<Int>): List<Int?> {
    val inputList = input.split(" ")
    val position = inputList.map { it.toIntOrNull() }
    for (i in position.indices) {
        if (inputList.size != 2 || position[i] == null) {
            println("The position input is invalid.")
            exitProcess(0)
        }
        if (position[i] !in 0..diffSize[i]) {
            println("The position input is out of range.")
            exitProcess(0)
        }
    }
    return position
}

fun validateRGBInput(inputData: String): List<Int?> {
    val inputList = inputData.split(" ")
    val colors = inputList.map { it.toIntOrNull() }
    for (color in colors) {
        if (inputList.size != 3 || color == null || color !in 0..255) {
            println("The transparency color input is invalid.")
            exitProcess(0)
        }
    }
    return colors
}

fun hasTranslucentTransparency(image: BufferedImage): Boolean? {
    return when (image.transparency) {
        Transparency.BITMASK -> false
        Transparency.OPAQUE -> false
        Transparency.TRANSLUCENT -> true
        else -> null
    }
}

fun blendImageAndWatermark(
    image: BufferedImage,
    watermark: BufferedImage,
    transpWeight: Int,
    useAlphaChannel: Boolean,
    transpColor: Color?,
    positionMethod: PositionMethods,
    position: List<Int?>?
): BufferedImage {
    val outputImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val imageColor = Color(image.getRGB(x, y))
            val (wmx, wmy) = if (positionMethod == PositionMethods.GRID) mutableListOf(
                x % watermark.width,
                y % watermark.height
            ) else if (positionMethod == PositionMethods.SINGLE) {
                val wmx = x - position!![0]!!
                val wmy = y - position[1]!!
                if (wmx in 0 until watermark.width && wmy in 0 until watermark.height) {
                    mutableListOf(wmx, wmy)
                } else {
                    mutableListOf(null, null)
                }
            } else mutableListOf(x, y)
            if (wmx == null && wmy == null) {
                outputImage.setRGB(x, y, imageColor.rgb)
            } else {
                val wmColor = Color(watermark.getRGB(wmx!!, wmy!!), true)
                if ((useAlphaChannel && wmColor.alpha == 0) || wmColor == transpColor) {
                    outputImage.setRGB(x, y, imageColor.rgb)
                } else {
                    val outputColor = Color(
                        (transpWeight * wmColor.red + (100 - transpWeight) * imageColor.red) / 100,
                        (transpWeight * wmColor.green + (100 - transpWeight) * imageColor.green) / 100,
                        (transpWeight * wmColor.blue + (100 - transpWeight) * imageColor.blue) / 100
                    )
                    outputImage.setRGB(x, y, outputColor.rgb)
                }
            }
        }
    }
    return outputImage
}

fun validateFilename(filename: String) {
    if (!filename.matches(IMAGE_EXTENSION_REGEXP)) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }
}

fun validateTranspValue(value: Int?) {
    if (value == null) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
    if (value !in 0..100) {
        println("The transparency percentage is out of range.")
        exitProcess(0)
    }
}

fun validateDimensions(filename1: String, filename2: String) {
    val image = ImageIO.read(File(filename1))
    val wmImage = ImageIO.read(File(filename2))
    if (image.height < wmImage.height || image.width < wmImage.width) {
        print("The watermark's dimensions are larger.")
        exitProcess(0)
    }
}

fun validateImageFile(filename: String, isWatermark: Boolean = false) {
    val obj = if (isWatermark) "watermark" else "image"
    val imageFile = File(filename)
    if (imageFile.exists()) {
        val image = ImageIO.read(imageFile)
        if (image.colorModel.numColorComponents != 3) {
            println("The number of $obj color components isn't 3.")
            exitProcess(0)
        }
        if (image.colorModel.pixelSize !in 24..32 step 8) {
            println("The $obj isn't 24 or 32-bit.")
            exitProcess(0)
        }
    } else {
        println("The file $filename doesn't exist.")
        exitProcess(0)
    }
}

fun getImageInfo(image: BufferedImage, filename: String): String {
    var info = "Image file: ${filename}"
    info += "\nWidth: ${image.width}"
    info += "\nHeight: ${image.height}"
    info += "\nNumber of components: ${image.colorModel.numComponents}"
    info += "\nNumber of color components: ${image.colorModel.numColorComponents}"
    info += "\nBits per pixel: ${image.colorModel.pixelSize}"
    info += "\nTransparency: ${
        when (image.transparency) {
            Transparency.BITMASK -> "BITMASK"
            Transparency.OPAQUE -> "OPAQUE"
            Transparency.TRANSLUCENT -> "TRANSLUCENT"
            else -> "not defined"
        }
    }"
    return info
}