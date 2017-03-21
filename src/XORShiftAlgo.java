public class XORShiftAlgo extends RNGAlgo {

    // Marsaglia's paper on XORShift --> https://www.jstatsoft.org/article/view/v008i14/xorshift.pdf
    // implemented here is a 32-bit generator with period 2^32 - 1

    private int a = 1, b = 3, c = 10;

    public XORShiftAlgo() {
        this(System.nanoTime());
    }

    public XORShiftAlgo(long seed) {
        state = seed;
    }

    public byte nextByte() {
        state ^= state << 13;
        state ^= state >> 17;
        state ^= state << 5;
        return (byte) state;
    }

}
