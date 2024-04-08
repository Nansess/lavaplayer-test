package com.sedmelluq.discord.lavaplayer.source.youtube2.track;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeFormatInfo;

import java.util.List;
import java.util.StringJoiner;

import static com.sedmelluq.discord.lavaplayer.container.Formats.MIME_AUDIO_WEBM;

public class YoutubeTrackFormats {
    private final List<YoutubeStreamFormat> formats;
    private final String playerScriptUrl;

    public YoutubeTrackFormats(List<YoutubeStreamFormat> formats, String playerScriptUrl) {
        this.formats = formats;
        this.playerScriptUrl = playerScriptUrl;
    }

    public List<YoutubeStreamFormat> getFormats() {
        return this.formats;
    }

    public String getPlayerScriptUrl() {
        return playerScriptUrl;
    }

    public YoutubeStreamFormat getBestFormat() {
        YoutubeStreamFormat bestFormat = null;

        for (YoutubeStreamFormat format : formats) {
            if (!format.isDefaultAudioTrack()) {
                continue;
            }

            if (isBetterFormat(format, bestFormat)) {
                bestFormat = format;
            }
        }

        if (bestFormat == null) {
            StringJoiner joiner = new StringJoiner(", ");
            formats.forEach(format -> joiner.add(format.getType().toString()));
            throw new IllegalStateException("No supported audio streams available, available types: " + joiner);
        }

        return bestFormat;
    }

    private static boolean isBetterFormat(YoutubeStreamFormat format, YoutubeStreamFormat other) {
        YoutubeFormatInfo info = format.getInfo();

        if (info == null) {
            return false;
        } else if (other == null) {
            return true;
        } else if (MIME_AUDIO_WEBM.equals(info.mimeType) && format.getAudioChannels() > 2) {
            // Opus with more than 2 audio channels is unsupported by LavaPlayer currently.
            return false;
        } else if (info.ordinal() != other.getInfo().ordinal()) {
            return info.ordinal() < other.getInfo().ordinal();
        } else if (format.isDrc() && !other.isDrc()) {
            // prefer non-drc formats
            // IF ANYTHING BREAKS/SOUNDS BAD, REMOVE THIS
            return false;
        } else {
            return format.getBitrate() > other.getBitrate();
        }
    }
}
