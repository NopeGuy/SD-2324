import java.io.*;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        String username = null;
        String choice = "";
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Connection c = new Connection(s);
        ClientDemultiplexer cd = new ClientDemultiplexer(c);
        cd.start();

        while(username == null){
            System.out.println("Bem vindo. \n1. Login\n2. Registar");
            String opt = stdin.readLine();
            if(opt.equals("1")) {
                System.out.print("Introduza o seu endereço de email: ");
                String email = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                cd.sendString(1, 0, email + ";;" + password);
                Frame f = cd.receive(1);
                if (new String(f.data).startsWith("ERROR")){
                    System.out.print(new String(f.data));
                } else {
                    System.out.println("Login com sucesso..");
                    username = email;
                }
            }
            if(opt.equals("2")){
                System.out.println("Introduza o seu username: ");
                String user = stdin.readLine();
                System.out.print("Introduza a sua palavra-passe: ");
                String password = stdin.readLine();
                cd.sendString(2, 0,user + ";;" + password);
                Frame f = cd.receive(1);
                if (new String(f.data).startsWith("ERROR")){
                    System.out.print(new String(f.data));
                } else {
                    System.out.println("Login com sucesso..");
                    username = user;
                }

            }
        }

        while(!choice.equals("9")){
            System.out.println(
                    "*MENU*\n" +
                            "1. Executar tarefa\n" +
                            "2. Ver estado das Tarefas\n" +
                            "3. Ver estado dos Servidores\n" +
                            "9. Sair"
            );

            choice = stdin.readLine();

            if (choice.equals("1")) {
                boolean tarefaEnviada = false;
                while(!tarefaEnviada) {
                    System.out.println("Introduza a localização do Programa: ");
                    String pn = stdin.readLine();

                        new Thread(() -> {
                            try {
                                FileInputStream fis = new FileInputStream(new File(pn));
                                cd.send(30, 0, fis.readAllBytes());
                                Frame respostaTarefa = cd.receive(30);
                                if(new String(respostaTarefa.data).startsWith("ERRO")){
                                    System.out.println("Erro do Servidor (tag="+respostaTarefa.tag+"): " + new String(respostaTarefa.data));
                                } else {
                                    System.out.printf("Tarefa com id = %d terminou. Resposta: ", respostaTarefa.taskid);
                                    System.out.println(new String(respostaTarefa.data));
                                }
                            } catch (Exception e){
                                System.out.println("Não foi possível enviar a tarefa: " + e.getMessage());
                            }
                        }).start();

                        tarefaEnviada = true;
                }
                System.out.println("Tarefa enviada para o servidor....");

            } else if( choice.equals("2")) {
                cd.send(40, 0, new byte[0]);
                System.out.println("Status do Servidor (ATUAL/MAX)\n" +
                        new String(cd.receive(40).data));
            } else if (choice.equals("3")) {

            } else if (choice.equals("9")){

            } else{
                System.out.println("Não existe nada com essa opção...");
            }
        }

        System.out.println("A terminar.");

    }
}