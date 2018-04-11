package ru.sbt.jschool.session6;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    public static void main(String[] args) throws IOException{
        String defaultPath = "src/main/resources/config.properties";
        Server server = new Server(args.length == 0 ? defaultPath : args[0]);
        server.start();
    }
    private String pathToProperties;

    public Server(String pathToProperties) {
        this.pathToProperties = pathToProperties;
    }

    public void start() throws IOException {
        Properties properties = getProperties();
        ServerSocket serverSocket = new ServerSocket(Integer.valueOf(properties.getProperty("port")));
        System.out.println(getFreeId(Paths.get(properties.getProperty("directory")), ".bin"));

/*       Path file = Paths.get(properties.getProperty("directory") + "/file.txt");
        List<String> lines = Arrays.asList("The first line", "The second line");
        Files.write(file, lines, Charset.forName("UTF-8"));*/

        while (true) {
            try (
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                String query = readInputHeaders(reader);
                createResponse(query, writer, Paths.get(properties.getProperty("directory")));
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
                .append("Server: java/1.8\r\n")
                .append("Content-Type: ")
                .append(contentType.toString())
                .append("\r\n")
                .append("Content-Length: ")
                .append(message.length())
                .append("\r\n")
                .append("Connection: keep-alive\r\n")
                .append("\r\n")
                .append(message);
        writer.write(response.toString());
        writer.flush();
    }

    private Properties getProperties(){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(pathToProperties));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private int getFreeId(Path path, String endsWith){
        File file = path.toFile();
        Set<Integer> ids = Arrays.stream(Objects.requireNonNull(file.list((dir, name) -> name.endsWith(endsWith))))
                .map(v -> Integer.valueOf(v.substring(0, v.length() - endsWith.length())))
                .collect(Collectors.toSet());
        for (int i = 0; true; i++) {
            if (!ids.contains(i)){
                return i;
            }
        }
    }

    private void createResponse(String query, BufferedWriter writer, Path path){
        try {
            if (query.startsWith("user")) {
                if (query.contains("create")){
                    Map<String, String> args = Arrays.stream(query.substring(query.indexOf('?')).split("&"))
                            .collect(Collectors.toMap(v -> v.split("=")[0], v -> v.split("=")[1]));
                    int id = saveUser(new User(args.get("name"), Integer.valueOf(args.get("age")), Integer.valueOf(args.get("salary"))), path);
                    sendResponse(writer, "ID " + id, Status.OK, ContentType.HTML);
                }
            }
            else{
                sendResponse(writer, "Unknown request", Status.NOT_FOUND, ContentType.HTML);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int saveUser(User user, Path path){
        int id = getFreeId(path, ".bin");
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toString() + "/" + id + ".bin"))) {
            out.writeObject(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }
}
