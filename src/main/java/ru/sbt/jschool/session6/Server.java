package ru.sbt.jschool.session6;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Server {
    public static final String PATH_TO_PROPERTIES = "src/main/resources/config.properties";
    public static void main(String[] args) throws IOException{
        Server server = new Server();
        server.start();
    }
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            try (
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                String query = readInputHeaders(reader);
                if (query.equals("error")){
                    sendResponse(writer, "Not found", Status.NOT_FOUND, ContentType.HTML);
                }else {
                    sendResponse(writer, query, Status.OK, ContentType.HTML);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        return dateFormat.format(calendar.getTime());
    }

    private String readInputHeaders(BufferedReader bufferedReader) {
        StringBuilder stringBuilder = new StringBuilder();
        String query = "";
        while (true) {
            try {
                String line = bufferedReader.readLine();
                if (line == null || line.trim().length() == 0) {
                    break;
                } else {
                    if (line.startsWith("GET")) {
                        query = line.substring(5, line.length() - 9);
                    }
                    stringBuilder.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(stringBuilder);
        return query;
    }

    private void sendResponse(BufferedWriter writer, String message, Status status, ContentType contentType) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ")
                .append(status.toString())
                .append("\r\n")
                .append("Date: ")
                .append(getServerTime())
                .append("\r\n")
                .append("Server: zzzadruga/0.0.1\r\n")
                .append("Content-Type: ")
                .append(contentType.toString())
                .append("\r\n")
                .append("Content-Length: ")
                .append(message.length())
                .append("\r\n")
                .append("Connection: keep-alive\r\n")
                .append("\r\n")
                .append(message.toUpperCase());
        writer.write(response.toString());
        writer.flush();
    }
}
