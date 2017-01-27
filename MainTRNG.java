import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

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
        new Thread(new SerialListener("COM3")).start();
        new Thread(new ByteWriter("geiger")).start();
    }

    private static class SerialListener implements Runnable {
        private SerialPort port;
        private int baudRate = 9600, timeout = 5000;
        private InputStream in;

        public SerialListener(String portName) {
            try {
                port = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open("Arduino", timeout);
                port.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                in = port.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        @Override
        public void run() {
            byte[] tempByte = new byte[8]; // "bytes" sent by arduino through serial are really as a series of 8 ASCII bytes
            String tempBinary;
            int index = 0;

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
                System.exit(-1);
            }
        }

        @Override
        public void run() {
            byte[] bytes;
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
        }
    }
}
