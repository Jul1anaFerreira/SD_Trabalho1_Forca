package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class HangmanServer {

    private static final int PORT = 12345;        // porto do servidor
    private static final int MAX_PLAYERS = 4;     // número máximo de jogadores

    private static long firstPlayerTime = 0;      // tempo de entrada do primeiro jogador
    private static boolean gameStarted = false;   // indica se o jogo já começou

    private static List<ClientHandler> players = new ArrayList<>(); // lista de jogadores
    private static Game game;

    public static void main(String[] args) throws Exception {

        // cria servidor TCP
        ServerSocket serverSocket = new ServerSocket(PORT);
        serverSocket.setSoTimeout(1000); // verifica ligações a cada 1 segundo

        System.out.println("Servidor iniciado...");
        System.out.println("À espera de jogadores...");

        while (true) {
            try {
                Socket socket = serverSocket.accept(); // aceita cliente

                // se jogo já começou rejeita ligação
                if (game != null && game.isStarted()) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("FULL");
                    socket.close();
                    continue;
                }

                // cria novo jogador
                ClientHandler client = new ClientHandler(socket, players.size() + 1);
                players.add(client);
                new Thread(client).start();

                // envia mensagem de boas-vindas
                client.send("WELCOME " + client.getId() + " " + players.size());

                System.out.println("Jogador " + players.size() + " entrou.");

                // guarda tempo do primeiro jogador
                if (players.size() == 1) {
                    firstPlayerTime = System.currentTimeMillis();
                }

            } catch (SocketTimeoutException e) {
                // usado apenas para continuar o loop
            }

            // lógica de início do jogo
            if (!gameStarted && players.size() >= 1) {
                long now = System.currentTimeMillis();

                // se atingir máximo começa o jogo
                if (players.size() == MAX_PLAYERS) {
                    gameStarted = true;
                    System.out.println("A iniciar jogo com 4 jogadores...");
                    startGame();
                }

                // se passar 20 segundos
                else if (now - firstPlayerTime >= 20000) {

                    // jogadores suficientes: começa jogo
                    if (players.size() >= 2) {
                        gameStarted = true;
                        System.out.println("A iniciar jogo com " + players.size() + " jogadores...");
                        startGame();
                    } 
                    
                    // apenas 1 jogador: termina
                    else {
                        System.out.println("Jogadores insuficientes.");

                        for (ClientHandler p : players) {
                            p.send("FULL");
                        }

                        players.clear();         // limpa jogadores
                        firstPlayerTime = 0;     // reset tempo
                    }
                }
            }
        }
    }

    // inicia o jogo numa nova thread
    private static void startGame() {
        game = new Game(players);
        new Thread(game).start();
    }
}