import java.io.*;
import java.util.Arrays;

public class Main {
    final static int BUFFER_SIZE = 32;
    final static int BUFFER = 1024;

    public static void main(String[] args) throws IOException {
        String file_name = "random";
        String file_name_w = "random1";
        File file = new File(file_name);
        long file_length = file.length();;
        int numberOfChunks = (int) Math.ceil((double)file_length / BUFFER_SIZE);

        Buffer<Chunk> readB = new Buffer<>(BUFFER_SIZE + BUFFER);
        Buffer<Chunk> writeB = new Buffer<>(BUFFER_SIZE + BUFFER);

        // creating Read and Write handlers
        ReadHandler rh = new ReadHandler(file_name, readB);
        WriteHandler wh = new WriteHandler(file_name_w, writeB, numberOfChunks);

        // creating Encryption handlers
        EncryptHandler eh1 = new EncryptHandler(readB, writeB);
        EncryptHandler eh2 = new EncryptHandler(readB, writeB);
        EncryptHandler eh3 = new EncryptHandler(readB, writeB);
        EncryptHandler eh4 = new EncryptHandler(readB, writeB);

        // creating threads for read and write
        Thread r = new Thread(rh);
        Thread w = new Thread(wh);

        // creating threads for encryption
        Thread e1 = new Thread(eh1);
        Thread e2 = new Thread(eh2);
        Thread e3 = new Thread(eh3);
        Thread e4 = new Thread(eh4);

        r.start();
        e1.start();
        e2.start();
        e3.start();
        e4.start();
        w.start();
    }
}
