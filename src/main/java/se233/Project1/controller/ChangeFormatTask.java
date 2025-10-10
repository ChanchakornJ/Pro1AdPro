package se233.Project1.controller;

import javafx.application.Platform;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

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
            boolean isVBR,
            File imageFile,
            Consumer<Double> progressCallback
    ) throws IOException {

       FFmpegProbeResult audioProbe = ffprobe.probe(inputPath);
        FFmpegFormat audioFormat = audioProbe.getFormat();
        double duration = audioFormat.duration;
        if (Double.isNaN(duration) || duration <= 0) {
            duration = 1;
        }
        final double totalDurationSeconds = duration;

        System.out.println("🔎 Probing: " + inputPath);
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new IOException("Input file does not exist: " + inputPath);
        }

        FFmpegBuilder builder = new FFmpegBuilder();
        if ("mp4".equalsIgnoreCase(format) && imageFile != null && imageFile.exists()) {
            builder.addInput(imageFile.getAbsolutePath());
            builder.addInput(inputPath);
        } else {
            builder.setInput(inputPath);
        }

        String outputFormat = format.toLowerCase();
        if (outputFormat.equals("m4a")) {
            outputFormat = "ipod";
        }

        FFmpegOutputBuilder output = builder.addOutput(outputPath)
                .setFormat(outputFormat)
                .setAudioChannels(channels)
                .setAudioSampleRate(sampleRate);

        if ("mp4".equalsIgnoreCase(format) && imageFile != null && imageFile.exists()) {
            output.setVideoCodec("libx264")
                    .setAudioCodec("aac")
                    .addExtraArgs(
                            "-shortest",
                            "-loop", "1",
                            "-tune", "stillimage",
                            "-pix_fmt", "yuv420p"
                    );
        } else {
            output.setAudioCodec(switch (format.toLowerCase()) {
                case "mp3" -> "libmp3lame";
                case "m4a", "aac" -> "aac";
                case "ogg" -> "libvorbis";
                case "flac" -> "flac";
                default -> "libmp3lame";
            });

            builder.addExtraArgs("-vn");
            if (!"flac".equalsIgnoreCase(format)) {
                if ("mp3".equalsIgnoreCase(format) && isVBR) {
                    output.addExtraArgs("-q:a", String.valueOf(bitrate));
                } else {
                    output.addExtraArgs("-b:a", bitrate + "k");
                }
            }
        }

        System.out.println("🔍 Input path: " + inputPath);
        System.out.println("🔍 Output path: " + outputPath);
        System.out.println("🔍 Format: " + format);
        if (imageFile != null) {
            System.out.println("🖼️ Using image: " + imageFile.getAbsolutePath());
        }

        output.done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegJob job = executor.createJob(builder, progress -> {
            if (progress.out_time_ns <= 0 || Double.isNaN(progress.out_time_ns)) return;

            double currentSeconds = progress.out_time_ns / 1_000_000_000.0;
            double percentage = currentSeconds / totalDurationSeconds;

            if (percentage > 1.0) percentage = 1.0;
            if (percentage < 0.0) percentage = 0.0;

            if (progressCallback != null) {
                final double finalPercentage = percentage; // ✅ Make it final
                Platform.runLater(() -> progressCallback.accept(finalPercentage));
            }
        });


        job.run();
    }
}
