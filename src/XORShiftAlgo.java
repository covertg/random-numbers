public class XORShiftAlgo extends RNGAlgo {

    // Marsaglia's paper on XORShift --> https://www.jstatsoft.org/article/view/v008i14/xorshift.pdf
    // implemented here is a 32-bit generator with period 2^32 - 1

    private int a = 13, b = 17, c = 5;

    public XORShiftAlgo() {
        this(System.nanoTime());
    }

    public XORShiftAlgo(long seed) {
        state = seed;
    }

    public byte nextByte() {
        state ^= state << a;
        state ^= state >> b;
        state ^= state << c;
        return (byte) state;
    }

}
