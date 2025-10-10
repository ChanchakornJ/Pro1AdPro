package se233.Project1.model;

public class FileSettings {
    public double bitrate;
    public double sampleRate;
    public int channels;
    public boolean isVBR;
    public String format;

    public FileSettings(double bitrate, double sampleRate, int channels) {
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.isVBR = false;
    }

    public FileSettings(double bitrate, double sampleRate, int channels, boolean isVBR) {
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.isVBR = isVBR;
    }

}
