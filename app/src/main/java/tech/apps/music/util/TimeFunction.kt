package tech.apps.music.util

fun songDuration(second: Long): String {
    var sec: Int = second.toInt()
    val hours: Int = (sec / 3600)
    sec -= hours * 3600
    val min = sec / 60
    sec -= min * 60

    var formattedTime = ""

    if (hours > 0) {
        if (hours < 10) formattedTime += "0"
        formattedTime += "$hours:"
    }

    if (min < 10) formattedTime += "0"
    formattedTime += "$min:"

    if (sec < 10) formattedTime += "0"
    formattedTime += sec

    return formattedTime
}

fun secondInFloatToTimeString(second: Float): String {
    var sec: Int = second.toInt()
    val hours: Int = (sec / 3600)
    sec -= hours * 3600
    val min = sec / 60
    sec -= min * 60

    var formattedTime = ""

    if (hours > 0) {
        if (hours < 10) formattedTime += "0"
        formattedTime += "$hours:"
    }

    if (min < 10) formattedTime += "0"
    formattedTime += "$min:"

    if (sec < 10) formattedTime += "0"
    formattedTime += sec

    return formattedTime
}
