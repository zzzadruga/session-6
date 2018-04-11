package ru.sbt.jschool.session6;

import ru.sbt.jschool.session6.JSONFormatter.JSONFormatter;
import ru.sbt.jschool.session6.JSONFormatter.JSONFormatterImpl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    public static void main(String[] args){
        String defaultPath = "src/main/resources/config.properties";
        Server server = new Server(args.length == 0 ? defaultPath : args[0]);
        server.start();
    }
    private String pathToProperties;

    public Server(String pathToProperties) {
        this.pathToProperties = pathToProperties;
    }

    public void start() {
        Properties properties = getProperties();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Integer.valueOf(properties.getProperty("port")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try (
                    Socket socket = Objects.requireNonNull(serverSocket).accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ) {
                String request = readRequest(reader);
                createResponse(request, writer, Paths.get(properties.getProperty("directory")), properties.getProperty("fileExtension"));
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

    private String readRequest(BufferedReader bufferedReader) {
        StringBuilder stringBuilder = new StringBuilder();
        String request = "";
        while (true) {
            try {
                String line = bufferedReader.readLine();
                if (line == null || line.trim().length() == 0) {
                    break;
                } else {
                    if (line.startsWith("GET")) {
                        request = line.substring(5, line.length() - 9);
                    }
                    stringBuilder.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // запись stringBuilder в лог
        return (request.endsWith("/") ? request.substring(0, request.length() - 1) : request);
    }

    private void sendResponse(BufferedWriter writer, String message, Status status, ContentType contentType) {
        try {
            String response = String
                    .format("HTTP/1.1 %s\r\n" +
                            "Date: %s\r\nServer: java/1.8\r\nContent-Type: %s\r\nContent-Length: %s\r\nConnection: keep-alive\r\n\n%s",
                    status.toString(), getServerTime(), contentType.toString(), message.length(), message);
            writer.write(response);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private int getFreeId(Path path, String fileExtension){
        File file = path.toFile();
        Set<Integer> ids =  getFileList(path, fileExtension)
                .stream()
                .map(v -> Integer.valueOf(v.getName().substring(0, v.getName().length() - fileExtension.length())))
                .collect(Collectors.toSet());
        for (int i = 0; true; i++) {
            if (!ids.contains(i)){
                return i;
            }
        }
    }

    private Set<File> getFileList(Path path, String fileExtension){
        return Arrays
                .stream(Objects.requireNonNull(path.toFile().listFiles()))
                .filter(v -> v.getName().endsWith(fileExtension)).collect(Collectors.toSet());
    }

    private void createResponse(String query, BufferedWriter writer, Path path, String fileExtension) {
        try {
            if (query.startsWith("user")) {
                JSONFormatter jsonFormatter = new JSONFormatterImpl();
                if (query.contains("create")){
                    Map<String, String> args = Arrays.stream(query.substring(query.indexOf('?') + 1).split("&"))
                            .collect(Collectors.toMap(v -> v.split("=")[0], v -> v.split("=")[1]));
                    int id = saveUser(new User(args.get("name"), Integer.valueOf(args.get("age")), Integer.valueOf(args.get("salary"))), path, fileExtension);
                    sendResponse(writer, "ID " + id, Status.OK, ContentType.HTML);
                } else if (query.contains("delete")) {
                    int id = Integer.valueOf(query.substring(query.lastIndexOf('/') + 1, query.length()));
                    if (deleteUser(id, path, fileExtension)){
                        sendResponse(writer, "Done", Status.OK, ContentType.HTML);
                    } else {
                        sendResponse(writer, "User not found", Status.NOT_FOUND, ContentType.HTML);
                    }
                } else if (query.contains("list")) {
                    sendResponse(writer, jsonFormatter.marshall(getUserList(path, fileExtension)), Status.OK, ContentType.JSON);
                } else {
                    int id = Integer.valueOf(query.substring(query.lastIndexOf('/') + 1, query.length()));
                    User user = getUser(path, id, fileExtension);
                    if (user == null){
                        sendResponse(writer, "User not found", Status.NOT_FOUND, ContentType.HTML);
                    } else{
                        sendResponse(writer, jsonFormatter.marshall(user), Status.OK, ContentType.JSON);
                    }
                }
            }
            else{
                sendResponse(writer, "Unknown request", Status.NOT_FOUND, ContentType.HTML);
            }
        } catch (Exception e) {
            sendResponse(writer, e.toString(), Status.NOT_FOUND, ContentType.HTML);
            e.printStackTrace();
        }
    }

    private int saveUser(User user, Path path, String fileExtension){
        int id = getFreeId(path, fileExtension);
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toString() + "/" + id + fileExtension))) {
            out.writeObject(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }

    private boolean deleteUser(int id, Path path, String fileExtension){
        File file = new File(path.toString() + "/" + id + fileExtension);
        return file.delete();
    }

    private Set<User> getUserList(Path path, String fileExtension){
        Set<File> fileSet = getFileList(path, fileExtension);
        Set<User> users = new HashSet<>();
        for(File file : fileSet) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                users.add((User)in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    private User getUser(Path path, int id, String fileExtension){
        File file = new File(path.toString() + "/" + id + fileExtension);
        if (file.isFile()){
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                return ((User)in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
