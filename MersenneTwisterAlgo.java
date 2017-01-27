public class MersenneTwisterAlgo extends RNGAlgo {

    private MersenneTwister_luke rand;

    public MersenneTwisterAlgo() {
        rand = new MersenneTwister_luke(System.nanoTime());
    }

    public MersenneTwisterAlgo(long seed) {
        rand = new MersenneTwister_luke(seed);
    }

    public byte nextByte() {
        return rand.nextByte();
    }

}