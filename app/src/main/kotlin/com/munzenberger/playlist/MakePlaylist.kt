package com.munzenberger.playlist

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class MakePlaylist : CliktCommand() {
    val directory by argument().path(
        mustExist = true,
        canBeFile = false
    )

    val album by option("--album")
        .default("Playlist")
        .help("Album name, written to ALBUM field")

    val albumArtist by option("--album-artist")
        .default("Various")
        .help("Album artist, written to ALBUM_ARTIST field")

    override fun run() {
        val files = Files
            .newDirectoryStream(directory, "*.mp3")
            .use { stream ->
                stream
                    .sortedBy { Files.readAttributes(it, BasicFileAttributes::class.java).creationTime() }
                    .toList()
            }

        files.forEachIndexed { index, file ->
            process(file, index + 1)
        }
    }

    private fun process(file: Path, track: Int) {
        println("Track $track: ${file.fileName}")

        val f = AudioFileIO.read(file.toFile())
        f.tag.apply {
            setField(FieldKey.TRACK, track.toString())
            setField(FieldKey.ALBUM, album)
            setField(FieldKey.ALBUM_ARTIST, albumArtist)
        }
        f.commit()
    }
}
