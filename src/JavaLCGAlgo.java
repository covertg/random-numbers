import java.util.Random;

public class JavaLCGAlgo extends RNGAlgo {

    // wrapper class for Java's Random, which uses an LCG. internally it has a 48-bit state, but only returns the last 32 bits

    private Random rand;
    private byte[] tempByte = new byte[1];

    public JavaLCGAlgo() {
        rand = new Random(); // Random has an enhanced seeding method using nanoTime()
    }

    public JavaLCGAlgo(long seed) {
        rand = new Random(seed);
    }

    public byte nextByte() {
        rand.nextBytes(tempByte);
        return tempByte[0]; // actually does the same thing as casting nextInt() to byte
         }

}