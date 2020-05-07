import java.io.*;
import java.util.Arrays;

public class Main {
    final static int BUFFER_SIZE = 32;
    final static int BUFFER = 1024;

    public static void main(String[] args) throws IOException {

        // getting arguments from command line
        if(args.length !=3){
            System.out.println("Must include 3 parameters");
            System.out.println("1) encrypt/decrypt");
            System.out.println("2) Name of the file to encrypt/decrypt");
            System.out.println("3) Name of the file containing the key");
            System.exit(-1);
        }


        String action = args[0];
        // check if action is inputted correctly
        if(!action.equals("encrypt") && !action.equals("decrypt")){
            System.out.println("Please write either encrypt or decrypt");
            System.exit(-1);
        }

        String file_name = args[1];
        String file_name_key = args[2];


        File file = new File(file_name);
        long file_length = file.length();;
        int numberOfChunks = (int) Math.ceil((double)file_length / BUFFER_SIZE);

        Buffer<Chunk> readB = new Buffer<>(BUFFER_SIZE + BUFFER);
        Buffer<Chunk> writeB = new Buffer<>(BUFFER_SIZE + BUFFER);

        // creating Read and Write handlers
        ReadHandler rh = new ReadHandler(file_name, readB);
        WriteHandler wh = new WriteHandler(file_name, writeB, numberOfChunks);

        // creating Encryption handlers
        EncryptHandler eh1 = new EncryptHandler(readB, writeB, file_name_key, action);
        EncryptHandler eh2 = new EncryptHandler(readB, writeB, file_name_key, action);
        EncryptHandler eh3 = new EncryptHandler(readB, writeB, file_name_key, action);
        EncryptHandler eh4 = new EncryptHandler(readB, writeB, file_name_key, action);

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
