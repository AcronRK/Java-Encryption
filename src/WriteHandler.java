import java.io.RandomAccessFile;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WriteHandler implements Runnable {
    final static int BUFFER_SIZE = 32;
    final static int BUFFER = 1024;
    private String file_name;
    Buffer<Chunk> writeBuffer ;

    int numberOfChunks;
    char[] text_visualization;

    public WriteHandler(String file_name, Buffer<Chunk> writeBuffer, int numberOfChunks) {
        this.file_name = file_name;
        this.writeBuffer = writeBuffer;
        this.numberOfChunks = numberOfChunks;
        // creating a string for visualization purposes
        // when we will encrypt part of the text file, the string will be updated
        text_visualization = new char[numberOfChunks];
        for(int i = 0 ; i < numberOfChunks; i++) {
            text_visualization[i] = 'D';
        }
    }

    @Override
    public void run() {
        while(true){
            Chunk c = writeBuffer.read();
            int pos = c.position;
            byte[] chunk = c.chunk;
            writeToFile(file_name, chunk, pos);

            // c.position increments with 32 because of the buffer size
            // to get the position of the chunk in the character array we have to divide by the buffer size
            text_visualization[c.position / 32] = 'E';
            System.out.println(text_visualization);

            // check if all the file has been encrypted or decrypted
            if(checkEnd(text_visualization))
                System.exit(-1);
        }

    }

    private synchronized void writeToFile(String file_name_w, byte[] chunk, int index) {
        try {
            RandomAccessFile raf = new RandomAccessFile(file_name_w, "rws");
            raf.seek(index);
            raf.write(chunk);
            raf.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    // this function will return true if all the file has been encrypted
    // Going to be used to terminate the program
    private boolean checkEnd(char[] text_visualization){
        for (char c : text_visualization) {
            if (c == 'D')
                return false;
        }
        return true;
    }

}
