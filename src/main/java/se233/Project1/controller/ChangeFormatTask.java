package se233.Project1.controller;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;

public class ChangeFormatTask {
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public ChangeFormatTask(String ffmpegPath, String ffprobePath) throws IOException {
        this.ffmpeg = new FFmpeg(ffmpegPath);
        this.ffprobe = new FFprobe(ffprobePath);
    }

    public void convertToMp3(String inputPath, String outputPath) throws IOException {
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputPath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .setFormat("mp3")
                .setAudioBitRate(192000)
                .setAudioChannels(2)
                .setAudioSampleRate(44100)
                .done();

        ffmpeg.run(builder);
    }
}

