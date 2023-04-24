package ws.schild.jave;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author z's'b
 * 该文件是自己改写ws.schild.jave.MultimediaInfo.java 文件，主要是为了解决ffmpeg.exe的路径问题 
 * 使用时注意包的位置
 */
public class MyMultimediaInfo {
    private final static Log LOG = LogFactory.getLog(MultimediaObject.class);
    /**
     * This regexp is used to parse the ffmpeg output about the size of a video
     * stream.
     */
    private static final Pattern SIZE_PATTERN = Pattern.compile(
            "(\\d+)x(\\d+)", Pattern.CASE_INSENSITIVE);
    /**
     * This regexp is used to parse the ffmpeg output about the frame rate value
     * of a video stream.
     */
    private static final Pattern FRAME_RATE_PATTERN = Pattern.compile(
            "([\\d.]+)\\s+(?:fps|tbr)", Pattern.CASE_INSENSITIVE);
    /**
     * This regexp is used to parse the ffmpeg output about the bit rate value
     * of a stream.
     */
    private static final Pattern BIT_RATE_PATTERN = Pattern.compile(
            "(\\d+)\\s+kb/s", Pattern.CASE_INSENSITIVE);
    /**
     * This regexp is used to parse the ffmpeg output about the sampling rate of
     * an audio stream.
     */
    private static final Pattern SAMPLING_RATE_PATTERN = Pattern.compile(
            "(\\d+)\\s+Hz", Pattern.CASE_INSENSITIVE);
    /**
     * This regexp is used to parse the ffmpeg output about the channels number
     * of an audio stream.
     */
    private static final Pattern CHANNELS_PATTERN = Pattern.compile(
            "(mono|stereo|quad)", Pattern.CASE_INSENSITIVE);

    /**
     * The locator of the ffmpeg executable used by this extractor.
     */
    private final FFMPEGLocator locator;

    private File inputFile;

    /**
     * It builds an extractor using a {@link DefaultFFMPEGLocator} instance to
     * locate the ffmpeg executable to use.
     *
     * @param input Input file for creating MultimediaObject
     */
    public MyMultimediaInfo(File input) {
        this.locator = new DefaultFFMPEGLocator();
        this.inputFile = input;

    }

    public File getFile() {
        return this.inputFile;
    }

    public void setFile(File file) {
        this.inputFile = file;
    }

    /**
     * It builds an extractor with a custom {@link FFMPEGLocator}.
     *
     * @param input   Input file for creating MultimediaObject
     * @param locator The locator picking up the ffmpeg executable used by the
     *                extractor.
     */
    public MyMultimediaInfo(File input, FFMPEGLocator locator) {
        this.locator = locator;
        this.inputFile = input;
    }

    /**
     * Returns a set informations about a multimedia file, if its format is
     * supported for decoding.
     *
     * @return A set of informations about the file and its contents.
     * @throws InputFormatException If the format of the source file cannot be
     *                              recognized and decoded.
     * @throws EncoderException     If a problem occurs calling the underlying
     *                              ffmpeg executable.
     */
    public MultimediaInfo getInfo(String url) throws InputFormatException,
            EncoderException {
        FFMPEGExecutor ffmpeg = locator.createExecutor();
        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(url);
        try {
            ffmpeg.execute();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        try {
            RBufferedReader reader = new RBufferedReader(new InputStreamReader(ffmpeg
                    .getErrorStream()));
            return parseMultimediaInfo(inputFile, reader);
        } finally {
            ffmpeg.destroy();
        }

    }

    /**
     * Private utility. It parses the ffmpeg output, extracting informations
     * about a source multimedia file.
     *
     * @param source The source multimedia file.
     * @param reader The ffmpeg output channel.
     * @return A set of informations about the source multimedia file and its
     * contents.
     * @throws InputFormatException If the format of the source file cannot be
     *                              recognized and decoded.
     * @throws EncoderException     If a problem occurs calling the underlying
     *                              ffmpeg executable.
     */
    private MultimediaInfo parseMultimediaInfo(File source,
                                               RBufferedReader reader) throws InputFormatException,
            EncoderException {
        Pattern p1 = Pattern.compile("^\\s*Input #0, (\\w+).+$\\s*",
                Pattern.CASE_INSENSITIVE);
        Pattern p2 = Pattern.compile(
                "^\\s*Duration: (\\d\\d):(\\d\\d):(\\d\\d)\\.(\\d\\d).*$",
                Pattern.CASE_INSENSITIVE);
        Pattern p3 = Pattern.compile(
                "^\\s*Stream #\\S+: ((?:Audio)|(?:Video)|(?:Data)): (.*)\\s*$",
                Pattern.CASE_INSENSITIVE);
        Pattern p4 = Pattern.compile(
                "^\\s*Metadata:",
                Pattern.CASE_INSENSITIVE);
        MultimediaInfo info = null;
        try {
            int step = 0;
            while (true) {
                String line = reader.readLine();
//                LOG.debug("Output line: " + line);
                if (line == null) {
                    break;
                }
                switch (step) {
                    case 0: {
                        String token = source.getAbsolutePath() + ": ";
                        if (line.startsWith(token)) {
                            String message = line.substring(token.length());
                            throw new InputFormatException(message);
                        }
                        Matcher m = p1.matcher(line);
                        if (m.matches()) {
                            String format = m.group(1);
                            info = new MultimediaInfo();
                            info.setFormat(format);
                            step++;
                        }
                        break;
                    }
                    case 1: {
                        Matcher m = p2.matcher(line);
                        if (m.matches()) {
                            long hours = Integer.parseInt(m.group(1));
                            long minutes = Integer.parseInt(m.group(2));
                            long seconds = Integer.parseInt(m.group(3));
                            long dec = Integer.parseInt(m.group(4));
                            long duration = (dec * 10L) + (seconds * 1000L)
                                    + (minutes * 60L * 1000L)
                                    + (hours * 60L * 60L * 1000L);
                            info.setDuration(duration);
                            step++;
                        } else {
                            // step = 3;
                        }
                        break;
                    }
                    case 2: {
                        Matcher m = p3.matcher(line);
                        Matcher m4 = p4.matcher(line);
                        if (m.matches()) {
                            String type = m.group(1);
                            String specs = m.group(2);
                            if ("Video".equalsIgnoreCase(type)) {
                                VideoInfo video = new VideoInfo();
                                StringTokenizer st = new StringTokenizer(specs, ",");
                                for (int i = 0; st.hasMoreTokens(); i++) {
                                    String token = st.nextToken().trim();
                                    if (i == 0) {
                                        video.setDecoder(token);
                                    } else {
                                        boolean parsed = false;
                                        // Video size.
                                        Matcher m2 = SIZE_PATTERN.matcher(token);
                                        if (!parsed && m2.find()) {
                                            int width = Integer.parseInt(m2
                                                    .group(1));
                                            int height = Integer.parseInt(m2
                                                    .group(2));
                                            video.setSize(new VideoSize(width,
                                                    height));
                                            parsed = true;
                                        }
                                        // Frame rate.
                                        m2 = FRAME_RATE_PATTERN.matcher(token);
                                        if (!parsed && m2.find()) {
                                            try {
                                                float frameRate = Float
                                                        .parseFloat(m2.group(1));
                                                video.setFrameRate(frameRate);
                                            } catch (NumberFormatException e) {
//                                                LOG.info("Invalid frame rate value: " + m2.group(1), e);
                                            }
                                            parsed = true;
                                        }
                                        // Bit rate.
                                        m2 = BIT_RATE_PATTERN.matcher(token);
                                        if (!parsed && m2.find()) {
                                            int bitRate = Integer.parseInt(m2
                                                    .group(1));
                                            video.setBitRate(bitRate * 1000);
                                            parsed = true;
                                        }
                                    }
                                }
                                info.setVideo(video);
                            } else if ("Audio".equalsIgnoreCase(type)) {
                                AudioInfo audio = new AudioInfo();
                                StringTokenizer st = new StringTokenizer(specs, ",");
                                for (int i = 0; st.hasMoreTokens(); i++) {
                                    String token = st.nextToken().trim();
                                    if (i == 0) {
                                        audio.setDecoder(token);
                                    } else {
                                        boolean parsed = false;
                                        // Sampling rate.
                                        Matcher m2 = SAMPLING_RATE_PATTERN
                                                .matcher(token);
                                        if (!parsed && m2.find()) {
                                            int samplingRate = Integer.parseInt(m2
                                                    .group(1));
                                            audio.setSamplingRate(samplingRate);
                                            parsed = true;
                                        }
                                        // Channels.
                                        m2 = CHANNELS_PATTERN.matcher(token);
                                        if (!parsed && m2.find()) {
                                            String ms = m2.group(1);
                                            if ("mono".equalsIgnoreCase(ms)) {
                                                audio.setChannels(1);
                                            } else if ("stereo"
                                                    .equalsIgnoreCase(ms)) {
                                                audio.setChannels(2);
                                            } else if ("quad"
                                                    .equalsIgnoreCase(ms)) {
                                                audio.setChannels(4);
                                            }
                                            parsed = true;
                                        }
                                        // Bit rate.
                                        m2 = BIT_RATE_PATTERN.matcher(token);
                                        if (!parsed && m2.find()) {
                                            int bitRate = Integer.parseInt(m2
                                                    .group(1));
                                            audio.setBitRate(bitRate * 1000);
                                            parsed = true;
                                        }
                                    }
                                }
                                info.setAudio(audio);
                            }
                        } else // if (m4.matches())
                        {
                            // Stay on level 2
                        }
                        /*
                            else
                            {
                            step = 3;
                            }
                         */
                        break;
                    }
                    default:
                        break;
                }
                if (line.startsWith("frame=")) {
                    reader.reinsertLine(line);
                    break;
                }
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        }
        if (info == null) {
            throw new InputFormatException();
        }
        return info;
    }
}
