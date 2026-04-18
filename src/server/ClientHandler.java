package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket socket;        // ligação ao cliente
    private BufferedReader in;   // recebe mensagens
    private PrintWriter out;     // envia mensagens
    private int id;              // id do jogador
    private String guess = "";   // jogada do jogador
 
    public ClientHandler(Socket socket, int id) throws IOException {
        this.socket = socket;
        this.id = id;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public int getId() {
        return id;
    }

    // envia mensagem ao cliente
    public void send(String msg) {
        out.println(msg);
    }

    // devolve a jogada 
    public synchronized String getGuess() {
        return guess;
    }

    // guarda a jogada
    public synchronized void setGuess(String guess) {
        this.guess = guess;
    }
    
    // fecha ligação
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {
            String line;

            // lê mensagens do cliente
            while ((line = in.readLine()) != null) {

                // se for uma jogada
                if (line.startsWith("GUESS")) {
                    setGuess(line.substring(6));
                }
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado");
        }
    }
}  