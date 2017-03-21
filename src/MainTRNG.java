import gnu.io.NRSerialPort; // external library https://github.com/NeuronRobotics/nrjavaserial

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;

public class MainTRNG {

    private static ArrayList<Byte> buffer; // stores received bytes before they're written
    private static ReentrantLock lock; // used so we're not writing to the buffer at the same time from different threads
    private static boolean running; // true until user input—threads stop once false

    public static void main(String[] args) {
        lock = new ReentrantLock(true);
        buffer = new ArrayList<>();
        running = true;

        // two threads: one reads from serial, one writes to file "geiger"
        new Thread(new SerialListener(args)).start();
        new Thread(new ByteWriter("geiger")).start();

        // wait for input. if user sends a q, it's time to close up
        char in = ' ';
        Scanner scanner = new Scanner(System.in);
        while (in != 'q' && in != 'Q') {
            in = scanner.next().charAt(0);
        }
        running = false;
    }

    private static class SerialListener implements Runnable {
        private NRSerialPort serial;
        private int baudRate = 9600;
        private InputStream in;

        public SerialListener(String[] args) {
            // ensure only 1 arg, assume that arg is the port name (i.e COM3, /dev/tty.usbmodem..., etc)
            if (args.length != 1) {
                System.out.println("Please specify port name.");
                for (String s : NRSerialPort.getAvailableSerialPorts()) {
                    System.out.println("Available port: " + s); // List possibilities, just to be nice
                }
                System.exit(1);
            }
            System.out.println("Connecting serial and opening file ... send 'q' to stop any time.");
            try {
                serial = new NRSerialPort(args[0], baudRate);
                serial.connect();
                in = serial.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(2);
            }
        }

        @Override
        public void run() {
            System.out.println("Serial connected.");
            byte[] tempByte = new byte[8]; // "bytes" sent by arduino through serial are really as a series of 8 ASCII bytes
            String tempBinary; // used to convert the ASCII bytes into integers
            int index = 0;
            while (running) {
                try {
                    if (in.available() > 0) {
                        tempByte[index++] = (byte) in.read(); // loop ascii byte by byte (that is, bit by bit)
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // once we have 8 bits, we generate a byte
                if (index >= 8) {
                    lock.lock(); // reentrantlock ensures the two threads aren't messing with buffer at the same time
                    try {
                        tempBinary = new String(tempByte);
                        System.out.println(tempBinary);
                        buffer.add((byte) Integer.parseInt(tempBinary, 2));
                    } finally {
                        lock.unlock();
                    }
                    index = 0;
                }
            }
            try {
                in.close();
                serial.disconnect();
                System.out.println("Serial disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ByteWriter implements Runnable {

        private OutputStream writer;
        private long count;

        public ByteWriter(String filename) {
            try {
                File file = new File("." + File.separatorChar + filename);
                if (!file.exists())
                    file.createNewFile();
                writer = new FileOutputStream(file, true);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(3);
            }
        }

        @Override
        public void run() {
            System.out.println("File opened for writing.");
            byte[] bytes;
            while (running) {
                if (buffer.size() > 0) {
                    // if SerialListener has written anything to the buffer, we lock and copy the buffer into a temporary bytes variable so we can write
                    lock.lock();
                    try {
                        bytes = new byte[buffer.size()];
                        for (int i = 0; i < buffer.size(); i++) {
                            bytes[i] = buffer.get(i);
                        }
                        buffer.clear(); // clear the buffer
                    } finally {
                        lock.unlock();
                    }
                    // once we have some bytes, let's write
                    try {
                        System.out.println("writing " + bytes.length + " bytes " + " (count: " + (count += bytes.length) + ")");
                        writer.write(bytes);
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // instead of checking the buffer EVERY.INSTANT we poll it every 2 seconds ... works just fine
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                writer.close();
                System.out.println("File closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
