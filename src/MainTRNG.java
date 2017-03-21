import gnu.io.NRSerialPort; // external library https://github.com/NeuronRobotics/nrjavaserial

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class MainTRNG {

    private static ArrayList<Byte> buffer; // stores received bytes before they're written
    private static ReentrantLock lock; // used so we're not writing to the buffer at the same time from different threads

    public static void main(String[] args) {
        lock = new ReentrantLock(true);
        buffer = new ArrayList<>();

        // two threads: one reads from serial, one writes to file "geiger"
        new Thread(new SerialListener(args)).start();
        new Thread(new ByteWriter("geiger")).start();
    }

    private static class SerialListener implements Runnable {
        private NRSerialPort serial;
        private int baudRate = 9600;
        private InputStream in;

        public SerialListener(String[] args) {
            if (args.length != 1)
            {
                System.out.println("Please specify port name.");
                for (String s: NRSerialPort.getAvailableSerialPorts())
                {
                    System.out.println("Available port: " + s);
                }
                System.exit(1);
            }

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
            byte[] tempByte = new byte[8]; // "bytes" sent by arduino through serial are really as a series of 8 ASCII bytes
            String tempBinary;
            int index = 0;
            try {
                while (true) {
                    try {
                        if (in.available() > 0) {
                            tempByte[index++] = (byte) in.read();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (index >= 8) {
                        lock.lock();
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
            } finally {
                try {
                    in.close();
                    serial.disconnect();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
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
            byte[] bytes;
            try {
                while (true) {
                    if (buffer.size() > 0) {
                        lock.lock();
                        try {
                            bytes = new byte[buffer.size()];
                            for (int i = 0; i < buffer.size(); i++) {
                                bytes[i] = buffer.get(i);
                            }
                            buffer.clear();
                        } finally {
                            lock.unlock();
                        }
                        try {
                            System.out.println("writing " + bytes.length + " bytes " + " (count: " + (count += bytes.length) + ")");
                            writer.write(bytes);
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                try {
                    writer.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
