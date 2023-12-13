package Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TasksExecutor implements Serializable {

    private final int MAX_MEMORY = 1 * (5000);
    private int ACTUAL_MEMORY = 0;
    private Queue<Task> taskQueue;
    private ReentrantLock readLock = new ReentrantLock();

    private ReentrantLock writeLock = new ReentrantLock();
    private Condition condition;

    private int taskId = 0;


    private class Task {

        public int taskID;
        public byte[] data;

        private Connection c;

        public Task(int id, byte[] programa, Connection c) {
            this.taskID = id;
            this.data = programa;
            this.c = c;
        }

        public void executeTask(){
            try{
                byte[] out = sd23.JobFunction.execute(this.data);
                this.c.sendData(31, taskID, out);
            } catch (IOException e) {
                // CONNECTION CLOSED Provavelment
            } catch (Exception exc) {
                // ERRO
            }


        }
    }

    public TasksExecutor() {
        taskQueue = new ArrayDeque<>();
        condition = readLock.newCondition();

        // Inicia a thread de execução
        Thread executorThread = new Thread(this::executeTasks);
        executorThread.start();
    }


    /*
     * Chamada para adicionar uma Task à queue
     */
    public void addTask(byte[] data, Connection c) {

        try {
            writeLock.lock();
            if (this.ACTUAL_MEMORY + data.length <= MAX_MEMORY) {
                this.ACTUAL_MEMORY += data.length;
                taskQueue.add(new Task(taskId++, data, c));
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
                    Task task = taskQueue.poll();
                    if(Client.DEBUG) System.out.printf("Thread %d vai executar a TaskID = %d\n", Thread.currentThread().getId(), task.taskID);
                    readLock.unlock();
                    task.executeTask();
                    this.ACTUAL_MEMORY -= task.data.length;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }


    }
}



