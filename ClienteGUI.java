package cliente;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClienteGUI extends JFrame {//criar a janela grafica
    private JTextArea resultadoArea;//area que a resposta vai aparecer
    private JButton enviarButton;//botão para escolher arquivo

    public ClienteGUI() {//executa quando a janela é criada
        setTitle("Cliente - Analisador de Texto");//titulo
        setSize(400, 300);//largura
        setDefaultCloseOperation(EXIT_ON_CLOSE);//x pra fechar
        setLayout(new BorderLayout());//define ode posicionar

        resultadoArea = new JTextArea();//variavel da area de texto-criar
        resultadoArea.setEditable(false);
        add(new JScrollPane(resultadoArea), BorderLayout.CENTER);

        enviarButton = new JButton("Selecionar e Enviar Arquivo");
        add(enviarButton, BorderLayout.SOUTH);//criar botão

        enviarButton.addActionListener(e -> escolherEEnviarArquivo());//quando o botão é criado chama esse metodo

        setVisible(true);
    }

    private void escolherEEnviarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            enviarArquivo(file);// chama a variavel com arq selecionado
        }
    }

    private void enviarArquivo(File file) {
        try {
            URL url = new URL("http://localhost:8080/processar");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain");//define o arquivo

            try (OutputStream os = conn.getOutputStream();
                 FileInputStream fis = new FileInputStream(file)) {//le o arquivo
                byte[] buffer = new byte[4096];//le em blocos de 4096 bytes
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {//le
                    os.write(buffer, 0, bytesRead);//envia pra o servidor
                }
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));//le a resposta do servidor depois de processar o arquivo
            String responseLine;
            StringBuilder response = new StringBuilder();

            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine).append("\n");//le linha por linha e quarda a respostas
            }
            in.close();

            resultadoArea.setText(response.toString());

        } catch (IOException ex) {
            ex.printStackTrace();
            resultadoArea.setText("Erro ao enviar arquivo: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClienteGUI::new);
    }
}
