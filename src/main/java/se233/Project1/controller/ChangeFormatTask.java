package se233.Project1.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import se233.Project1.model.ConversionException;

import java.io.*;
import java.util.function.Consumer;

public class ChangeFormatTask extends Task<Void> {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final String ffmpegPath;
    private final String ffprobePath;

    private final String inputPath;
    private final String outputPath;
    private final String format;
    private final int bitrate;
    private final int channels;
    private final int sampleRate;
    private final boolean isVBR;
    private final File mediaFile;

    public ChangeFormatTask(String ffmpegPath,
                            String ffprobePath,
                            String inputPath,
                            String outputPath,
                            String format,
                            int bitrate,
                            int channels,
                            int sampleRate,
                            boolean isVBR,
                            File mediaFile) throws IOException {

        this.ffmpegPath = ffmpegPath;
        this.ffprobePath = ffprobePath;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.format = format;
        this.bitrate = bitrate;
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.isVBR = isVBR;
        this.mediaFile = mediaFile;

        this.ffmpeg = new FFmpeg(ffmpegPath);
        this.ffprobe = new FFprobe(ffprobePath);
    }

    @Override
    protected Void call() throws Exception {
        convertToFormatWithProgress(inputPath, outputPath, format, bitrate, channels, sampleRate, isVBR, mediaFile,
                progress -> updateProgress(progress, 1.0));
        return null;
    }

    public void convertToFormatWithProgress(
            String inputPath,
            String outputPath,
            String format,
            int bitrate,
            int channels,
            int sampleRate,
            boolean isVBR,
            File mediaFile,
            Consumer<Double> progressCallback
    ) throws IOException, ConversionException {

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new ConversionException("Input file does not exist: " + inputPath);
        }

        FFmpegProbeResult probe = ffprobe.probe(inputPath);
        FFmpegFormat meta = probe.getFormat();
        double duration = (meta != null && meta.duration > 0) ? meta.duration : -1;
        final double totalDuration = duration;

        boolean isVideo = false;
        if (mediaFile != null && mediaFile.exists()) {
            String name = mediaFile.getName().toLowerCase();
            isVideo = name.endsWith(".mp4") || name.endsWith(".mov") ||
                    name.endsWith(".avi") || name.endsWith(".mkv");
        }

        // CASE 1: MP4 output
        if ("mp4".equalsIgnoreCase(format) && mediaFile != null && mediaFile.exists()) {

            String command;
            if (isVideo) {
                command = String.format(
                        "%s -y -i \"%s\" -i \"%s\" -map 0:v:0 -map 1:a:0 -c:v copy -c:a aac -shortest -b:a %dk \"%s\"",
                        ffmpegPath,
                        mediaFile.getAbsolutePath(),
                        inputPath,
                        bitrate,
                        outputPath
                );
            } else {
                command = String.format(
                        "%s -y -loop 1 -i \"%s\" -i \"%s\" -c:v libx264 -c:a aac -shortest -tune stillimage -pix_fmt yuv420p -b:a %dk \"%s\"",
                        ffmpegPath,
                        mediaFile.getAbsolutePath(),
                        inputPath,
                        bitrate,
                        outputPath
                );
            }

            System.out.println("🎥 Executing command: " + command);

            try {
                ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !isCancelled()) {
                        if (line.contains("time=")) {
                            String time = line.substring(line.indexOf("time=") + 5).split(" ")[0];
                            double seconds = parseTimeToSeconds(time);
                            if (totalDuration > 0 && progressCallback != null) {
                                double percent = Math.min(seconds / totalDuration, 1.0);
                                progressCallback.accept(percent);
                            }
                        }
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) throw new IOException("FFmpeg exited with code " + exitCode);
                if (progressCallback != null)
                    progressCallback.accept(-1.0);
                return;
            } catch (Exception e) {
                System.err.println("⚠️ Manual ffmpeg merge failed: " + e.getMessage());
                if (progressCallback != null)
                    progressCallback.accept(-1.0);
                throw new ConversionException("MP4 merge failed", e);
            }
        }

        // CASE 1.5: Audio-only MP4
        if ("mp4".equalsIgnoreCase(format) && (mediaFile == null || !mediaFile.exists())) {
            String command = String.format(
                    "%s -y -i \"%s\" -vn -c:a aac -b:a %dk -progress pipe:1 -nostats \"%s\"",
                    ffmpegPath,
                    inputPath,
                    bitrate,
                    outputPath
            );

            System.out.println("Executing audio-only MP4 command: " + command);

            try {
                ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !isCancelled()) {
                        if (line.startsWith("out_time_ms=") || line.startsWith("out_time=")) {
                            double seconds;
                            if (line.startsWith("out_time_ms=")) {
                                double currentMs = Double.parseDouble(line.substring("out_time_ms=".length()));
                                seconds = currentMs / 1_000_000.0;
                            } else {
                                String t = line.substring("out_time=".length()).trim();
                                seconds = parseTimeToSeconds(t);
                            }

                            if (totalDuration > 0 && progressCallback != null) {
                                double percent = Math.min(seconds / totalDuration, 1.0);
                                progressCallback.accept(1.0);
                            }
                        }
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) throw new IOException("FFmpeg exited with code " + exitCode);
                if (progressCallback != null)
                    progressCallback.accept(1.0);
                return;
            } catch (Exception e) {
                System.err.println("⚠️ MP4 (audio-only) conversion failed: " + e.getMessage());
                if (progressCallback != null)
                    progressCallback.accept(-1.0);
                throw new ConversionException("MP4 (audio-only) conversion failed", e);
            }
        }

        // CASE 2: Audio-only formats
        String codec = switch (format.toLowerCase()) {
            case "mp3" -> "libmp3lame";
            case "m4a", "aac" -> "aac";
            case "ogg" -> "libvorbis";
            case "flac" -> "flac";
            default -> "libmp3lame";
        };

        String bitrateArg = isVBR
                ? String.format("-q:a %d", Math.max(0, Math.min(5, bitrate)))
                : String.format("-b:a %dk", bitrate);

        String outputContainer = format.equalsIgnoreCase("m4a") ? "mp4" : format;

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputPath)
                .overrideOutputFiles(true)
                .addOutput(outputPath)
                .setFormat(outputContainer)
                .setAudioChannels(channels)
                .setAudioSampleRate(sampleRate)
                .addExtraArgs("-vn")
                .addExtraArgs("-c:a", codec)
                .addExtraArgs("-progress", "pipe:1")
                .addExtraArgs("-nostats")
                .addExtraArgs(bitrateArg.split(" ")[0], bitrateArg.split(" ")[1])
                .done();

        System.out.println("   Starting conversion:");
        System.out.println("   Input: " + inputPath);
        System.out.println("   Format: " + format);
        System.out.println("   Bitrate: " + bitrate + " kbps");
        System.out.println("   VBR: " + isVBR);
        System.out.println("   Output: " + outputPath);

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegJob job = executor.createJob(builder, progress -> {
            if (progress.out_time_ns <= 0 || Double.isNaN(progress.out_time_ns)) return;
            if (totalDuration <= 1 || Double.isNaN(totalDuration)) return;

            double seconds = progress.out_time_ns / 1_000_000_000.0;
            double percent = Math.min(seconds / totalDuration, 1.0);

            if (progressCallback != null)
                progressCallback.accept(percent);
        });

        job.run();
        if (progressCallback != null)
            progressCallback.accept(1.0);
}

        private static double parseTimeToSeconds(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length < 3) return 0;
            double h = Double.parseDouble(parts[0]);
            double m = Double.parseDouble(parts[1]);
            double s = Double.parseDouble(parts[2]);
            return h * 3600 + m * 60 + s;
        } catch (Exception e) {
            return 0;
        }
    }
}
