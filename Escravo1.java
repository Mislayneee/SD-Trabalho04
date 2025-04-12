import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;

public class Escravo1 {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/letras", new LetrasHandler());
        server.setExecutor(null);

        System.out.println("🔵 Escravo de letras rodando na porta 8081");

        server.start();
    }

    static class LetrasHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder texto = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                texto.append(linha);
            }

            long qtdLetras = texto.chars().filter(Character::isLetter).count();
            String resposta = "Quantidade de letras: " + qtdLetras;

            exchange.sendResponseHeaders(200, resposta.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(resposta.getBytes());
            os.close();
        }
    }
}
