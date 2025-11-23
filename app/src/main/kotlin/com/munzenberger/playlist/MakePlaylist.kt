package com.munzenberger.playlist

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.path

class MakePlaylist : CliktCommand() {
    val directory by argument().path(
        mustExist = true,
        canBeFile = false
    )

    override fun run() {
        println("hello world!")
    }
}
