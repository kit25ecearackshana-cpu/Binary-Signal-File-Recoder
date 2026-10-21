import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Binary Signal File Recorder
 * Generates binary signal samples, saves them in a binary file,
 * reads them back, and verifies that no sample was lost or changed.
 */
public class BinarySignalFileRecorder {
    private static final String FILE_NAME = "signal_samples.dat";
    private static final int SAMPLE_COUNT = 20;

    public static void main(String[] args) {
        int[] samples = generateSamples(SAMPLE_COUNT, 42L);

        System.out.println("=== Binary Signal File Recorder ===");
        System.out.println("Generated samples: " + Arrays.toString(samples));

        try {
            writeSamples(samples, FILE_NAME);
            int[] restoredSamples = readSamples(FILE_NAME);

            System.out.println("Read-back samples: " + Arrays.toString(restoredSamples));
            boolean verified = Arrays.equals(samples, restoredSamples);
            System.out.println("Verification: " + (verified ? "PASS - no data lost" : "FAIL - data mismatch"));

            if (!verified) {
                System.err.println("Error: recorded data differs from original samples.");
                System.exit(1);
            }
            System.out.println("File created: " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("File I/O error: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Invalid binary file: " + e.getMessage());
        }
    }

    // Creates repeatable 0/1 signal samples for demonstration.
    private static int[] generateSamples(int count, long seed) {
        int[] samples = new int[count];
        Random random = new Random(seed);
        for (int i = 0; i < count; i++) {
            samples[i] = random.nextBoolean() ? 1 : 0;
        }
        return samples;
    }

    // Writes the number of samples followed by each sample as an integer.
    private static void writeSamples(int[] samples, String fileName) throws IOException {
        try (DataOutputStream output = new DataOutputStream(
                new FileOutputStream(fileName))) {
            output.writeInt(samples.length);
            for (int sample : samples) {
                if (sample != 0 && sample != 1) {
                    throw new IllegalArgumentException("A binary sample must be 0 or 1.");
                }
                output.writeInt(sample);
            }
        }
    }

    // Reads the count and samples from the binary file.
    private static int[] readSamples(String fileName) throws IOException {
        try (DataInputStream input = new DataInputStream(
                new FileInputStream(fileName))) {
            int count = input.readInt();
            if (count < 0 || count > 1_000_000) {
                throw new IllegalStateException("Sample count is outside the allowed range.");
            }

            int[] samples = new int[count];
            for (int i = 0; i < count; i++) {
                try {
                    int sample = input.readInt();
                    if (sample != 0 && sample != 1) {
                        throw new IllegalStateException("Found a value other than 0 or 1.");
                    }
                    samples[i] = sample;
                } catch (EOFException e) {
                    throw new EOFException("File ended before all samples were read.");
                }
            }
            return samples;
        }
    }
            }
