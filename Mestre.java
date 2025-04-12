import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Mestre {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/processar", new ProcessarHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));

        System.out.println("🟢 Servidor Mestre rodando na porta 8080");
        System.out.println("🧪 Aguardando requisições do cliente...");

        server.start();
    }

    static class ProcessarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream is = exchange.getRequestBody();
            StringBuilder conteudo = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String linha;
            while ((linha = reader.readLine()) != null) {
                conteudo.append(linha).append("\n");
            }

            ExecutorService executor = Executors.newFixedThreadPool(2);

            System.out.println("📤 Enviando dados para os escravos...");
            Callable<String> tarefaLetras = () -> enviarParaEscravo("http://escravo1:8081/letras", conteudo.toString(), "Escravo 1 (Letras)");
            Callable<String> tarefaNumeros = () -> enviarParaEscravo("http://escravo2:8082/numeros", conteudo.toString(), "Escravo 2 (Números)");

            Future<String> resultadoLetras = executor.submit(tarefaLetras);
            Future<String> resultadoNumeros = executor.submit(tarefaNumeros);

            try {
                System.out.println("⏳ Aguardando respostas dos escravos...");
                String letras = resultadoLetras.get();
                String numeros = resultadoNumeros.get();

                String resposta = "Resultado final da analise:\n\n" + letras + "\n" + numeros;

                System.out.println("✅ Todos os escravos responderam com sucesso.");
                exchange.sendResponseHeaders(200, resposta.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(resposta.getBytes());
                os.close();
            } catch (Exception e) {
                String erro = "❌ Erro no processamento: " + e.getMessage();
                System.out.println(erro);
                exchange.sendResponseHeaders(500, erro.getBytes().length);
                exchange.getResponseBody().write(erro.getBytes());
                exchange.close();
            }
        }

        private String enviarParaEscravo(String urlStr, String conteudo, String nomeEscravo) {
            try {
                System.out.println("➡️ Conectando ao " + nomeEscravo + " em " + urlStr);
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "text/plain");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(conteudo.getBytes());
                }

                System.out.println("📨 Dados enviados para " + nomeEscravo + ". Recebendo resposta...");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder resposta = new StringBuilder();
                String linha;
                while ((linha = in.readLine()) != null) {
                    resposta.append(linha).append("\n");
                }
                in.close();

                System.out.println("✅ Resposta recebida do " + nomeEscravo);
                return resposta.toString();
            } catch (IOException e) {
                String erro = "❌ Erro ao contactar " + nomeEscravo + " (" + urlStr + "): " + e.getMessage();
                System.out.println(erro);
                return erro + "\n";
            }
        }
    }
}
