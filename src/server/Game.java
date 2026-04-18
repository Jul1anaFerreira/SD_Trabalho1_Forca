package server;

import java.util.*;

public class Game implements Runnable {

	private List<ClientHandler> players;   // lista de jogadores
    private String word;                  // palavra a adivinhar
    private char[] mask;                 // palavra escondida 
    private int attempts = 6;            // tentativas restantes
    private boolean started = false;     
    private Set<Character> used = new HashSet<>(); // letras já usadas

    public Game(List<ClientHandler> players) {
        this.players = players;
        this.word = getRandomWord(); // escolhe palavra aleatória
        this.mask = "_".repeat(word.length()).toCharArray();
    }

    public boolean isStarted() {
        return started;
    }

    private String getRandomWord() {
    	String[] words = {
    		    "gato", "cao", "elefante", "leao", "tigre",
    		    "girafa", "zebra", "macaco", "cavalo", "ovelha",
    		    "porco", "galinha", "pato", "coelho", "urso",
    		    "raposa", "lobo", "canguru", "pinguim", "golfinho",

    		    "carro", "mota", "bicicleta", "comboio", "aviao",
    		    "barco", "autocarro", "carrinha", "metro", "taxi",

    		    "casa", "apartamento", "elevador", "escada", "porta",
    		    "janela", "telhado", "quarto", "cozinha", "sala",
    		    "garagem", "varanda", "jardim", "piscina", "corredor",

    		    "mesa", "cadeira", "sofa", "cama", "armario",
    		    "espelho", "tapete", "candeeiro", "prateleira", "televisao",

    		    "banana", "maca", "pera", "laranja", "morango",
    		    "melancia", "uva", "abacaxi", "kiwi", "cereja",

    		    "arroz", "massa", "pizza", "hamburguer", "sopa",
    		    "salada", "pao", "queijo", "ovo", "carne",

    		    "escola", "professor", "aluno", "caderno", "caneta",
    		    "lapis", "mochila", "livro", "teste", "exame",

    		    "praia", "montanha", "rio", "lago", "floresta",
    		    "deserto", "ilha", "cidade", "aldeia", "estrada"
    		};
        return words[new Random().nextInt(words.length)];
    }

    @Override
    public void run() {
        started = true;
        
         // envia estado inicial
        broadcast("START " + new String(mask) + " " + attempts + " 20000");

        int round = 1;

        while (attempts > 0) {
        	// início de ronda
            broadcast("ROUND " + round + " " + new String(mask) + " " + attempts + " " + getUsedLetters());

            long start = System.currentTimeMillis();
            long timeout = 20000;
          
            // espera até todos jogarem ou timeout
            while (System.currentTimeMillis() - start < timeout) {

                boolean allPlayed = true;

                for (ClientHandler p : players) {
                    String g = p.getGuess();
                    if (g == null || g.isEmpty()) {
                        allPlayed = false;
                        break;
                    }
                }

                if (allPlayed) break;

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {}
            }

            List<Integer> winners = new ArrayList<>();
            boolean roundFailed = false;
            
            // processa jogadas
            for (ClientHandler p : players) {
                String g = p.getGuess();

                // não jogou: falha
                if (g == null || g.isEmpty()) {
                    roundFailed = true;
                    continue;
                }
                // tentativa de letra
                if (g.length() == 1) {
                    char c = Character.toLowerCase(g.charAt(0));
                    used.add(c);

                    if (word.indexOf(c) >= 0) {
                        for (int i = 0; i < word.length(); i++) {
                            if (word.charAt(i) == c) {
                                mask[i] = c;
                            }
                        }
                    } 
                    else {
                        roundFailed = true;
                    }
                } 
                
                // tentativa de palavra
                else {
                    if (g.equalsIgnoreCase(word)) {
                        winners.add(p.getId());
                    } 
                    else {
                        roundFailed = true;
                    }
                }

                p.setGuess("");
            }
            
            // se ninguém contribuiu perde tentativa
            if (roundFailed) {
                attempts--;
            }

            // vitória
            if (new String(mask).equals(word) || !winners.isEmpty()) {
                if (winners.isEmpty()) {
                    for (ClientHandler p : players) {
                        winners.add(p.getId());
                    }
                }
               
                broadcast("END WIN " + winners + " " + word);
                
                // fecha ligações                           
                for (ClientHandler p : players) {
                    p.close();
                }

                return;
            }
            
            // derrota
            if (attempts <= 0) {
            	broadcast("END LOSE " + word);

            	for (ClientHandler p : players) {
            	    p.close();
            	}
            	return;
            }
            round++;
        }
    }

    // envia mensagem a todos os jogadores
    private void broadcast(String msg) {
        for (ClientHandler p : players) {
            p.send(msg);
        }
    }

    // pausa a execução da thread durante um certo tempo (ms)
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
    
    // devolve letras usadas
    private String getUsedLetters() {
        StringBuilder sb = new StringBuilder();
        for (char c : used) {
            sb.append(c);
        }
        return sb.toString();
    }
}