package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class HangmanClient {

    public static void main(String[] args) throws Exception {

        // liga ao servidor
        Socket socket = new Socket("localhost", 12345);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Scanner sc = new Scanner(System.in);

        // thread para receber mensagens do servidor
        new Thread(() -> {
            try {
                String line;

                while ((line = in.readLine()) != null) {

                    // mensagem de boas-vindas
                    if (line.startsWith("WELCOME")) {
                        String[] parts = line.split(" ");
                        System.out.println("=================================");
                        System.out.println("Bem-vindo Jogador " + parts[1]);
                        System.out.println("=================================");
                    }

                    // início do jogo
                    else if (line.startsWith("START")) {
                        String[] parts = line.split(" ");
                        System.out.println("\n=== JOGO INICIADO ===");
                        System.out.println("Palavra: " + formatWord(parts[1]));
                        System.out.println("Tentativas: " + parts[2]);
                        System.out.println("Tempo por ronda: " + (Integer.parseInt(parts[3]) / 1000) + "s");
                    }

                    // nova ronda
                    else if (line.startsWith("ROUND")) {
                        String[] parts = line.split(" ");

                        System.out.println("\n========================");
                        System.out.println("RONDA " + parts[1]);
                        System.out.println("Palavra: " + formatWord(parts[2]));
                        System.out.println("Tentativas: " + parts[3]);

                        // letras já usadas
                        if (parts.length > 4) {
                            System.out.println("Letras usadas: " + formatWord(parts[4]));
                        } else {
                            System.out.println("Letras usadas: []");
                        }

                        System.out.println("========================");
                        System.out.print("Escreve uma letra ou palavra: ");
                    }

                    // vitória
                    else if (line.startsWith("END WIN")) {
                        String[] parts = line.split(" ");
                        System.out.println("\nJOGO TERMINADO - VITÓRIA!");
                        System.out.println("Vencedores: Jogador" + parts[2]);
                        System.out.println("Palavra correta: " + parts[3]);
                        System.exit(0);
                    }

                    // servidor cheio / ligação fechada
                    else if (line.equals("FULL")) {
                        System.out.println("\nLigação encerrada pelo servidor.");
                        System.exit(0);
                    }

                    // derrota
                    else if (line.startsWith("END LOSE")) {
                        String[] parts = line.split(" ");
                        System.out.println("\nJOGO TERMINADO - DERROTA!");
                        System.out.println("Palavra correta: " + parts[2]);
                        System.exit(0);
                    }
                }

            } catch (IOException e) {
            }
        }).start();

        // envio de jogadas
        while (true) {
            String input = sc.nextLine();
            out.println("GUESS " + input);
        }
    }

    // formata palavra 
    private static String formatWord(String word) {
        return String.join(" ", word.split(""));
    }
}