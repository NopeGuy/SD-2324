package Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Servers implements Serializable {

    private final HashMap<Integer, ServerConfig> database;
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();

    private class ServerConfig implements Serializable {
        public String hostname;
        public int MAX_MEMORY;
        public int PORT;

        public int id;

        public ServerConfig(int id, String host, int MAX_MEMORY, int PORT){
            this.hostname = host;
            this.id = id;
            this.MAX_MEMORY = MAX_MEMORY;
            this.PORT = PORT;
        }
    }
    public Servers() {
        this.database = new HashMap<>();
    }

    /*
     * Adiciona um servidor ao Sistema
     */
    public void addServer(String host, int port, int MAX_MEMORY){
        try {
            this.l.writeLock().lock();
            int newId = this.database.size();
            this.database.put(newId, new ServerConfig(newId, host, MAX_MEMORY, port));
        } finally {
            this.l.writeLock().unlock();
        }
    }

    /*
     * Remove um servidor do Sistema
     */
    public void removeServer(int i){
        try {
            this.l.writeLock().lock();
            this.database.remove(i);
        } finally {
            this.l.writeLock().unlock();
        }
    }

    @Override
    public String toString(){
        try {
            this.l.readLock().lock();
            String out = "";
            for (Map.Entry<Integer, ServerConfig> entry : this.database.entrySet()) {
                out += entry.getKey() + ". " + entry.getValue().hostname + "\n";
            }

            return out;
        } finally {
            this.l.readLock().unlock();
        }
    }

    /*
     * Serialize para o Ficheiro de Servidores
     */
    public void serialize(String fileLocation) throws IOException {
        try {
            //this.l.writeLock().lock();
            FileOutputStream fileStream = new FileOutputStream(fileLocation);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileStream);
            outputStream.writeObject(this);
            outputStream.close();
            fileStream.close();
        } finally {
            //this.l.writeLock().unlock();
        }

    }

    /*
     * Lê do ficheiro para a Class
     */
    public static Servers deserialize(String fileLocation) throws IOException, ClassNotFoundException {
        FileInputStream fileStream = new FileInputStream(fileLocation);
        ObjectInputStream inputStream = new ObjectInputStream(fileStream);
        Servers accounts = (Servers) inputStream.readObject();
        inputStream.close();
        fileStream.close();
        return accounts;
    }
}
