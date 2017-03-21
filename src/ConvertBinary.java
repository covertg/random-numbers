import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ConvertBinary {

    // Takes a binary file and creates an encoded text file from the data. Helpful for working with data in excel/etc

    public static void main(String[] args) {
        if (args.length != 1)
        {
            return;
        }

        try {
            byte[] data = Files.readAllBytes(new File(args[0]).toPath());
            FileWriter writer = new FileWriter("." + File.separatorChar + "bytestext");
            System.out.println("Read " + data.length + " bytes from " + args[0]);
            int i = 0;
            for (byte b : data) {
                writer.write((b & 0xFF) + "\n");
                if (i++ % 100000 == 0)
                {
                    writer.flush();
                }
            }
            System.out.println("Closing, wrote " + i + " numbers");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
