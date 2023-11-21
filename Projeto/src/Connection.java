import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Connection {

    private DataInputStream is;
    private DataOutputStream os;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private Socket socket;
    public Connection(Socket s) throws IOException {
        this.socket = s;
        this.is = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        this.os = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }


    public void sendData(int tag, byte[] data) throws IOException {
        this.send(new Frame(tag, data));
    }
    /*
     * Envia a frame para o cliente
     */
    public void send(Frame f) throws IOException {
        try {
            this.rwLock.writeLock().lock();
            // Escreve o número da operaçao primeiro
            this.os.write(f.tag);
            // Escreve a quantidade de bytes que vao ser enviados
            this.os.write(f.data.length);
            // Envia os bytes
            this.os.write(f.data);
            // Finaliza e envia
            this.os.flush();
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    /*
     * Receber Frame
     */
    public Frame receive() throws IOException {
        int tag;
        byte[] data;
        try {
            this.rwLock.readLock().lock();
            tag = this.is.readInt();
            int n = this.is.readInt();
            data = new byte[n];
            this.is.readFully(data);
        }
        finally {
            this.rwLock.readLock().unlock();
        }
        return new Frame(tag,data);
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
