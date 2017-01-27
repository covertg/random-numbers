public class LinearCongruentialAlgo extends RNGAlgo {

    /* other parameters worth trying ...
        1103515245, 12345, 0x7FFFFFFF
        0x5deece66dL, 11, 0x1000000000000L (drand48 - but snub the first 16 bits)
        16807, 0, 2147483647 (old c++ minstd)
     */

    private long mult, inc, mod;

    public LinearCongruentialAlgo(long mult, long inc, long mod) {
        this(mult, inc, mod, System.nanoTime());
    }

    public LinearCongruentialAlgo(long mult, long inc, long mod, long seed) {
        this.mult = mult;
        this.inc = inc;
        this.mod = mod;
        state = seed;
    }

    public byte nextByte() {
        state = (mult * state + inc) % mod;
        return (byte) state;
    }

}