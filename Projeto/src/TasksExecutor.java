import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TasksExecutor implements Serializable {

    private final int MAX_MEMORY = 1 * (1000000);
    private int ACTUAL_MEMORY = 0;
    private final Queue<Task> taskQueue;
    private final ReentrantLock lock;
    private final Condition condition;

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
                this.c.sendData(30, taskID, out);
            } catch (IOException e) {
                // CONNECTION CLOSED Provavelment
            } catch (Exception exc) {
                // ERRO
            }


        }
    }

    public TasksExecutor() {
        taskQueue = new ArrayDeque<>();
        lock = new ReentrantLock();
        condition = lock.newCondition();

        // Inicia a thread de execução
        Thread executorThread = new Thread(this::executeTasks);
        executorThread.start();
    }

    public void addTask(byte[] data, Connection c) {
        lock.lock();
        try {
            if (this.ACTUAL_MEMORY + data.length <= MAX_MEMORY) {
                this.ACTUAL_MEMORY += data.length;
                taskQueue.add(new Task(taskId++, data, c));
                condition.signal();
            } else {
                System.out.println("Tarefa não adicionada: excede a memória disponível");
            }
            condition.signal(); // Sinaliza à thread que há novas tarefas
        } finally {
            lock.unlock();
        }
    }

    public byte[] getStatus() {
        return (this.ACTUAL_MEMORY + "/" + this.MAX_MEMORY).getBytes();
    }
    private void executeTasks() {
            while (true) {
                lock.lock();
                try {
                    while (taskQueue.isEmpty()) {
                        // Aguarda até que haja tarefas na fila
                        condition.await();
                    }
                    // Quando há tarefas, executa o comando
                    Task task = taskQueue.poll();
                    task.executeTask();
                    this.ACTUAL_MEMORY -= task.data.length;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    lock.unlock();
                }
            }


    }
}



