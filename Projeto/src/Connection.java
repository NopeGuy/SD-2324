import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Connection implements AutoCloseable {

    private DataInputStream is;
    private DataOutputStream os;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private Socket socket;
    public Connection(Socket s) throws IOException {
        this.socket = s;
        this.is = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        this.os = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }


    public void sendData(int tag, int taskid, byte[] data) throws IOException {
        this.send(new Frame(tag, taskid, data));
    }

    public void sendString(int tag, int taskid, String info) throws IOException{
        this.send(new Frame(tag, taskid, info.getBytes()));
    }


    /*
     * Envia a frame para o cliente
     */
    public void send(Frame f) throws IOException {
        try {
            //this.rwLock.writeLock().lock();
            // Escreve o número da operaçao primeiro
            this.os.writeInt(f.tag);
            // Escreve a taskid
            this.os.writeInt(f.taskid);
            // Escreve a quantidade de bytes que vao ser enviados
            this.os.writeInt(f.data.length);
            // Envia os bytes
            this.os.write(f.data);
            // Finaliza e envia
            this.os.flush();
        } finally {
            // this.rwLock.writeLock().unlock();
        }
    }

    /*
     * Receber Frame
     */
    public Frame receive() throws IOException {
        int tag;
        byte[] data;
        int taskid;
        int mem;
        try {
            this.rwLock.readLock().lock();
            tag = this.is.readInt();
            taskid = this.is.readInt();
            int nBytes = this.is.readInt();
            data = new byte[nBytes];
            this.is.readFully(data);
        }
        finally {
            this.rwLock.readLock().unlock();
        }
        return new Frame(tag,taskid, data);
    }

    /*
     * Termina a conexão com o cliente
     */
    public void close() throws IOException {
        this.is.close();
        this.os.close();
        this.socket.close();
    }


}
