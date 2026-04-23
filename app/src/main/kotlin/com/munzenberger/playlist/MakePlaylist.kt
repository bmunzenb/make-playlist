package com.munzenberger.playlist

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

typealias SortSelector<T> = (Path) -> T

sealed class SortStrategy<T>(val strategy: SortSelector<T>) {
    data object CreatedDate : SortStrategy<FileTime>(
        strategy = {
            Files.readAttributes(it, BasicFileAttributes::class.java).creationTime()
        }
    )
}

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

    val sortStrategy by option("--sort")
        .choice(
            "CreatedDate" to SortStrategy.CreatedDate
        )
        .default(SortStrategy.CreatedDate)
        .help("File attribute to sort by when creating the playlist")

    override fun run() {
        val files = Files
            .newDirectoryStream(directory, "*.mp3")
            .use { stream ->
                stream
                    .sortedBy(sortStrategy.strategy)
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
