public class AdditiveFeedbackAlgo extends RNGAlgo {

    // an implementation of glibc's random() function. uses an LCG to seed itself, and then works like a linear feedback
    // shift register. see http://www.mathstat.dal.ca/~selinger/random/ for an analysis
    // glibc source: http://sourceware.org/git/?p=glibc.git;a=tree

    private long[] state;
    private int insertPos;

    public AdditiveFeedbackAlgo() {
        this(System.nanoTime());
    }

    public AdditiveFeedbackAlgo(long seed) {
        state = new long[32];
        state[0] = seed;
        for (int i = 1; i < 31; i++) {
            state[i] = (0x41a7 * state[i - 1]) % 0x7fffffff;
            if (state[i] < 0)
                state[i] += 2147483647; // 0x7fffffff, or 2^31 - 1
        }
        state[31] = state[0];
        state[0] = state[1];
        state[1] = state[2];
        insertPos = 3;
        for (int i = 0; i < 310; i++) {
            nextByte();
        }
    }

    public byte nextByte() {
        try {
            state[insertPos] = state[Math.abs(insertPos - 3) % 32] + state[-1 * (insertPos - 31) % 32];
            return (byte) ((state[insertPos] >> 1) & 0x7fffffff); // snub the least random bit and end up with a 31-bit number
        } finally {
            if (insertPos <= 30)
                insertPos++;
            else
                insertPos = 0;
        }
    }
}
