import java.io.*;

public class ReadHandler implements Runnable{
    final static int BUFFER_SIZE = 32;
    final static int BUFFER = 1024;
    private String file_name;
    Buffer<Chunk> readBuffer;

    public ReadHandler(String file_name, Buffer<Chunk> readBuffer){
        this.file_name = file_name;
        this.readBuffer = readBuffer;
    }

    @Override
    public void run() {
        File file = new File(file_name);
        long file_length = file.length();
        for(int i = 0; i < (file_length / BUFFER_SIZE) + 1; i++){
            int chunk_pos = i * BUFFER_SIZE;
            byte[] chunk = getChunk(file, chunk_pos);
        Chunk c = new Chunk(chunk_pos, chunk);
        readBuffer.write(c);
        }
    }

    public synchronized byte[] getChunk(File file, long start)  {
        long length = file.length();
        long count = Math.min(BUFFER_SIZE, length - start);
        byte[] array = new byte[(int) count];
        try{
            InputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.skip(start);
            array = bis.readNBytes((int) count);
            bis.close();
        }catch (IOException r){
            System.err.println(r.toString());
        }
        return array;
    }
}
