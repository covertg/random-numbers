import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainPRNG {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        writeRandomNumbers("mersenne", new MersenneTwisterAlgo(1));
        writeRandomNumbers("additive-glibc-random", new AdditiveFeedbackAlgo(1));
        writeRandomNumbers("xorshift", new XORShiftAlgo(1));

        writeRandomNumbers("lcg-randu", new LinearCongruentialAlgo(65539, 0, 0x80000000, 1));
        writeRandomNumbers("lcg-unix-rand", new LinearCongruentialAlgo(1103515245, 12345, 0x80000000, 1));
        writeRandomNumbers("lcg-c++minstd", new LinearCongruentialAlgo(48271, 0, 2147483647, 1));
        writeRandomNumbers("lcg-java", new JavaLCGAlgo(1));

        System.out.println("Total time elapsed: " + (System.currentTimeMillis() - start) / 1000.0d + "s");
    }

    private static void writeRandomNumbers(String filename, RNGAlgo rng) {
        OutputStream writer;
        File file;
        try {
            file = new File("." + File.separatorChar + filename);
            writer = new FileOutputStream(file);
            if (!file.exists())
                file.createNewFile();
            long start = System.currentTimeMillis();
            byte[] bytes = genRandomNumbers(rng);
            System.out.println(filename + " completed in " + (System.currentTimeMillis() - start) / 1000.0 + "s");
            writer.write(bytes);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] genRandomNumbers(RNGAlgo rng) {
        byte[] bytes = new byte[10000000];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = rng.nextByte();
        }
        return bytes;
    }
}
