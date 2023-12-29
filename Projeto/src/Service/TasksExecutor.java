package Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TasksExecutor implements Serializable {

    private int MAX_MEMORY = 1 * (5000);
    private int ACTUAL_MEMORY = 0;
    private Queue<Task> taskQueue;
    private ReentrantLock readLock = new ReentrantLock();

    private ReentrantLock writeLock = new ReentrantLock();

    private ReentrantLock executionLock = new ReentrantLock();
    private Condition condition;

    private int taskId = 0;




    public TasksExecutor() {
        taskQueue = new ArrayDeque<>();
        condition = readLock.newCondition();

        // Inicia a thread de execução
        Thread executorThread = new Thread(this::executeTasks);
        executorThread.start();
    }

    /*
     * Future proof para Cloud
     */
    public void setMAX_MEMORY(int newMaxMemory){ this.MAX_MEMORY = newMaxMemory; }


    /*
     * Chamada para adicionar uma Task à queue
     */
    public void addTask(byte[] data, Connection c) {

        try {
            writeLock.lock();
            if (data.length < MAX_MEMORY) {
                //this.ACTUAL_MEMORY += data.length;
                //taskQueue.add(new Task(taskId++, data, c));
                readLock.lock();
                condition.signal();
                readLock.unlock();
                return;
            } else {
                c.sendData(31, 0, "ERRO: SEM MEMORIA".getBytes());
            }
            condition.signal(); // Sinaliza à thread que há novas tarefas
        } catch (Exception e){}
        finally {
            writeLock.unlock();
        }
    }

    public byte[] getStatus() {
        String resp = "Memória: " + this.ACTUAL_MEMORY + "/" + this.MAX_MEMORY +
                    "\nNúmero de Processos a Aguardar: " + this.taskQueue.size();
        return resp.getBytes();
    }
    private void executeTasks() {
            while (true) {
                if(Client.DEBUG) System.out.printf("Thread %d à espera de readLock\n", Thread.currentThread().getId());
                readLock.lock();
                if(Client.DEBUG) System.out.printf("Thread %d conseguiu o lock em executeTasks\n", Thread.currentThread().getId());
                try {
                    while (taskQueue.isEmpty()) {
                        if(Client.DEBUG) System.out.printf("Thread %d awaiting\n", Thread.currentThread().getId());
                        // Aguarda até que haja tarefas na fila
                        condition.await();
                    }
                    // Quando há tarefas, executa o comando
                    List<Task> listaTasks = new ArrayList<>();
                    if(Client.DEBUG) System.out.printf("Existem %d tasks por começar...", taskQueue.size());
                    //while(!taskQueue.isEmpty() && (listaTasks.isEmpty() ||
                        //    listaTasks.stream().mapToDouble(Task::getLength).sum() + this.ACTUAL_MEMORY < this.MAX_MEMORY))
                       // if(taskQueue.peek() != null && taskQueue.peek().getLength() + this.ACTUAL_MEMORY < this.MAX_MEMORY)
                        //    listaTasks.add(taskQueue.poll());
                    List<Thread> threads = new ArrayList<>();
                    for(Task t : listaTasks) threads.add(new Thread(t::executeTask));
                    if(Client.DEBUG) System.out.printf("A começar %d threads...", threads.size());
                    for(Thread tr : threads) tr.start();
                    for(Thread tr : threads) tr.join();

                    readLock.unlock();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }


    }
}



