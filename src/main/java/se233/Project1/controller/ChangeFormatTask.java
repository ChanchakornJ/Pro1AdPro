package se233.Project1.controller;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.bramp.ffmpeg.FFmpegExecutor;

import java.io.IOException;
import java.util.function.DoubleConsumer;

public class ChangeFormatTask {
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public ChangeFormatTask(String ffmpegPath, String ffprobePath) throws IOException {
        this.ffmpeg = new FFmpeg(ffmpegPath);
        this.ffprobe = new FFprobe(ffprobePath);
    }

    public void convertToFormatWithProgress(
            String inputPath,
            String outputPath,
            String format,
            int bitrate,
            int channels,
            int sampleRate,
            DoubleConsumer progressCallback
    ) throws IOException {

        var probeResult = ffprobe.probe(inputPath);
        double totalDurationSec = probeResult.getFormat().duration;

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputPath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .setFormat(format)
                .setAudioBitRate(bitrate * 1000L)
                .setAudioChannels(channels)
                .setAudioSampleRate(sampleRate)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        ProgressListener listener = new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                double currentSec = progress.out_time_ns / 1_000_000_000.0;
                double percent = currentSec / totalDurationSec;
                if (progressCallback != null) {
                    progressCallback.accept(Math.min(percent, 1.0));
                }
            }
        };

        FFmpegJob job = executor.createJob(builder, listener);
        job.run();
    }
}
