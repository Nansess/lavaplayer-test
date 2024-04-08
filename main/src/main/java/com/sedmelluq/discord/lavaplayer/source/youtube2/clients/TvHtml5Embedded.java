package com.sedmelluq.discord.lavaplayer.source.youtube2.clients;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeClientConfig;
import com.sedmelluq.discord.lavaplayer.source.youtube2.CannotBeLoaded;
import com.sedmelluq.discord.lavaplayer.source.youtube2.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube2.clients.skeleton.StreamingNonMusicClient;
import com.sedmelluq.discord.lavaplayer.source.youtube2.track.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.List;

public class TvHtml5Embedded extends StreamingNonMusicClient {
    protected YoutubeClientConfig baseConfig = new YoutubeClientConfig()
        .withApiKey("AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8")
        .withClientName("TVHTML5_SIMPLY_EMBEDDED_PLAYER")
        .withClientField("clientVersion", "2.0")
        .withThirdPartyEmbedUrl("https://www.youtube.com");

    @Override
    protected YoutubeClientConfig getBaseClientConfig(HttpInterface httpInterface) {
        return baseConfig.copy();
    }

    @Override
    protected JsonBrowser extractPlaylistVideoList(JsonBrowser json) {
        return json.get("contents")
            .get("sectionListRenderer")
            .get("contents")
            .index(0)
            .get("playlistVideoListRenderer");
    }

    @Override
    protected void extractPlaylistTracks(JsonBrowser json, List<AudioTrack> tracks, YoutubeAudioSourceManager source) {
        if (!json.get("contents").isNull()) {
            json = json.get("contents");
        }

        if (json.isNull()) {
            return;
        }

        for (JsonBrowser track : json.values()) {
            JsonBrowser item = track.get("videoRenderer");
            JsonBrowser authorJson = item.get("shortBylineText");

            // this client doesn't appear to receive "isPlayable" fields.
            // author is null -> video is region blocked
            if (!authorJson.isNull()) {
                String videoId = item.get("videoId").text();
                JsonBrowser titleField = item.get("title");
                String title = titleField.get("simpleText").textOrDefault(titleField.get("runs").index(0).get("text").text());
                String author = authorJson.get("runs").index(0).get("text").textOrDefault("Unknown artist");
                long duration = Units.secondsToMillis(item.get("lengthSeconds").asLong(Units.DURATION_SEC_UNKNOWN));

                AudioTrackInfo info = new AudioTrackInfo(title, author, duration, videoId, false, WATCH_URL + videoId);
                tracks.add(new YoutubeAudioTrack(info, source));
            }
        }
    }

    @Override
    public String getPlayerParams() {
        return WEB_PLAYER_PARAMS;
    }

    @Override
    public boolean canHandleRequest(String identifier) {
        // loose check to avoid loading playlists.
        // this client does support them, but it seems to be missing fields
        // that could be the difference between playable and unplayable --
        // notably the "isPlayable" field.
        // I'm also cautious of routing a lot of traffic through this client.
        // There is overridden code above but that's mostly just for reference.
        return (!identifier.contains("list=") || identifier.contains("list=RD")) && super.canHandleRequest(identifier);
    }

    @Override
    public String getIdentifier() {
        return baseConfig.getName();
    }

    @Override
    public AudioItem loadPlaylist(YoutubeAudioSourceManager source, HttpInterface httpInterface, String playlistId, String selectedVideoId) throws CannotBeLoaded {
        throw new UnsupportedOperationException();
    }
}
