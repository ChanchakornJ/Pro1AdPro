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


    public void convertToFormat(String inputPath, String outputPath, String format, int bitrate) throws IOException {
        System.out.println("🚀 Running ffmpeg: " + inputPath + " → " + outputPath + " (" + format + ")");

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputPath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .setFormat(format)
                .setAudioBitRate(bitrate * 1000L)
                .setAudioChannels(2)
                .setAudioSampleRate(44100)
                .done();

        ffmpeg.run(builder);
    }


}

