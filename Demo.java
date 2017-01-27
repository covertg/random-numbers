public class Demo {

    public static void main(String[] args) {
        int[] missed = new int[256];
        for (int i = 0; i < 256; i++) {
            missed[i] = i; // will be used to let us know if the algo doesn't actually generate certain numbers 0-255
        }

        RNGAlgo rng = new XORShiftAlgo(1);
        long sum = 0;
        long n = 100000;
        for (int i = 0; i < n; i++) {
            int b = rng.nextByte() & 0xFF; // byte ranges from -128 to 127, we want 0-255
//            System.out.println(b);
            for (int k = 0; k < 256; k++) {
                if (b == missed[k]) {
                    missed[k] = -1;
                }
            }
            sum += b;
        }

        for (int k = 0; k < 256; k++) {
            if (missed[k] != -1)
                System.out.println("Missed: " + k);
        }

        System.out.println("Mean: " + (double) sum / n); // should approach 127.5 (255/2)
    }
}