import java.io.*;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EncryptHandler implements Runnable{

    static byte[] key;


    String ACTION;
    final static int SHIFT = 1;
    final static int[] permutation = {6, 3, 16, 11, 7, 14, 8, 5, 15, 1, 2, 4, 13, 9, 10, 12};
    // the index of the first array is the value at the index of the second array
    final static int[] permutation_reversed = {10, 11, 2, 12, 8, 1, 5, 7, 14, 15, 4, 16, 13, 6, 9, 3};
    Buffer<Chunk> readBuffer;
    Buffer<Chunk> writeBuffer;
    static Lock lock = new ReentrantLock();


    public EncryptHandler(Buffer<Chunk> readBuffer, Buffer<Chunk> writeBuffer, String file_name_key, String action){
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;
        this.ACTION = action;

        //reading the key from the file
        {
            try {
                // import the key from the file
                key = getKey(file_name_key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() {

        while (true){
            lock.lock();
                Chunk c = readBuffer.read();
                int chunk_position = c.position;
                byte[] chunk = c.chunk;

            // check if file has to be encrypted or decrypted
            if(ACTION.equals("encrypt"))
                encrypt(chunk);
            else // decrypt
                decrypt(chunk);

            Chunk newChunk = new Chunk(chunk_position, chunk);
                writeBuffer.write(newChunk);
            lock.unlock();
        }
    }

    public static byte[] xor(byte[] chunk, byte[] key){
        for(int i = 0; i < chunk.length; i++){
            chunk[i] = (byte) (chunk[i] ^ key[i]);
        }
        return chunk;
    }

    public static byte[] rotateKeyLeft(byte[] key, int shift){
        byte[] temp = new byte[key.length];
        for(int i = 0; i < key.length; i++){
            temp[i] = (byte)(((key[i] & 0xff) << shift) | ((key[i] & 0xff) >>> (8 - shift)));
        }
        return temp;
    }

    public static byte[] rotateKeyRight(byte[] key, int shift){
        byte[] temp = new byte[key.length];
        for(int i = 0; i < key.length; i++){
            temp[i] = (byte)(((key[i] & 0xff)  >>> shift) | ((key[i] & 0xff) << (8 - shift)));
        }
        return temp;
    }

    public static byte[] substituteEncrypt(byte[] chunk){
        for(int i = 0; i < chunk.length; i++){
            chunk[i] = (byte) (chunk[i] * 137 % 256);
        }
        return chunk;
    }


    public static byte[] substituteDecrypt(byte[] chunk){
        for(int i = 0; i < chunk.length; i++){
            chunk[i] = (byte) (chunk[i] * 185 % 256);
        }
        return chunk;
    }

    public static byte[] permute(byte[] chunk, boolean isEncrypt){
        for(int i = 0; i < chunk.length; i+=2) {
            String bits1 = Integer.toBinaryString((chunk[i]));
            String bits2 = Integer.toBinaryString((chunk[i+1]));

            while(bits1.length() < 8){
                bits1 = "0" + bits1;
            }
            while(bits2.length() < 8){
                bits2 = "0" + bits2;
            }

            // in case of negative numbers when they get to infinity
            // ex. 111111111111101010101
            bits1 = bits1.substring(bits1.length()-8);
            bits2 = bits2.substring(bits2.length()-8);

            String bits = bits1 + bits2;
            String chunk1 = "";

            for(int j = 0; j < bits.length(); j++){
                // if we are encrypting we are going to use the permutation array
                if (isEncrypt)
                    chunk1 += bits.charAt(permutation[j]-1);
                // for decryption we are going to use the reverse array of permutation
                else
                    chunk1 += bits.charAt(permutation_reversed[j]-1);
            }

            // divide the bytes into 2
            bits1 = chunk1.substring(0, 8);
            bits2 = chunk1.substring(8);
            byte toBytes1;
            byte toBytes2;
            // if byte starts with 1 it means its negative
            // replace the 1 with 0 and subtract 128 convert it
            if(bits1.startsWith("1")){
                bits1 = bits1.replaceFirst("1", "0");
                toBytes1 = (byte) (Byte.parseByte(bits1, 2) - 128);
            }else{
                toBytes1 = (Byte.parseByte(bits1, 2));
            }

            // same for the second byte
            if(bits2.startsWith("1")){
                bits2 = bits2.replaceFirst("1", "0");
                toBytes2 = (byte) (Byte.parseByte(bits2, 2) - 128);
            }else{
                toBytes2 = (Byte.parseByte(bits2, 2));
            }

            //replace the bytes to the original byte array
            chunk[i] = toBytes1;
            chunk[i+1] = toBytes2;
        }
        return chunk;
    }

    public static byte[] encrypt(byte[] chunk){
        for(int i = 0; i < 256; i++) {
            xor(chunk, key);
            key = rotateKeyLeft(key, SHIFT);
            System.out.println(Arrays.toString(key));
            substituteEncrypt(chunk);
            permute(chunk, true);
        }
        //updateKey("key", key);
        return chunk;
    }

    public static byte[] decrypt(byte[] chunk){
        for(int i = 0; i < 256; i++) {
            permute(chunk, false);
            substituteDecrypt(chunk);
            key = rotateKeyRight(key, SHIFT);
            xor(chunk, key);
        }
        //updateKey("key", key);
        return chunk;
    }

    // this function is used to retrieve the key from a file containing a 256bit key
    public static byte[] getKey(String file_path) throws IOException {
        // the key is a 256 bit block, so in other words 32bytes
        byte[] key;
        InputStream fis = new FileInputStream(file_path);
        BufferedInputStream bis = new BufferedInputStream(fis);
        key = bis.readAllBytes();
        return key;
    }

    // this function is used to update the file containing the key when we rotate they key left or right
    public static void updateKey(String file_path, byte[] key) throws IOException {
        System.out.println(Arrays.toString(key));
        FileOutputStream fos = new FileOutputStream(file_path);
        fos.write(key);
    }
}
