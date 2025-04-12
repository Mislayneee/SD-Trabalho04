import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;

public class Escravo2 {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        server.createContext("/numeros", new NumerosHandler());
        server.setExecutor(null);

        System.out.println("🟣 Escravo de números rodando na porta 8082");

        server.start();
    }

    static class NumerosHandler implements HttpHandler {
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

            long qtdNumeros = texto.chars().filter(Character::isDigit).count();
            String resposta = "Quantidade de numeros: " + qtdNumeros;

            exchange.sendResponseHeaders(200, resposta.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(resposta.getBytes());
            os.close();
        }
    }
}
