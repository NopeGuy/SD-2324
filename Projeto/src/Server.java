import sd23.JobFunction;
import sd23.JobFunctionException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {


    public Server(Socket socket) {
        
    }

    @Override
    public void run() {
        try {
            // obter a tarefa de ficheiro, socket, etc...
            byte[] job = new byte[1000];

            // executar a tarefa
            byte[] output = JobFunction.execute(job);

            // utilizar o resultado ou reportar o erro
            System.err.println("success, returned "+output.length+" bytes");
        } catch (JobFunctionException e) {
            System.err.println("job failed: code="+e.getCode()+" message="+e.getMessage());
        }
    }
}
