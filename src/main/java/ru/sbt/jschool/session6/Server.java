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
    public static void main(String[] args) {
        String defaultPath = "src/main/resources/config.properties";
        Server server = new Server(args.length == 0 ? defaultPath : args[0]);
        server.start();
    }

    private String pathToProperties;

    public Server(String pathToProperties) {
        this.pathToProperties = pathToProperties;
    }

    public void start() {
        ApplicationProperties properties = getProperties();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(properties.getIntProperty("port"));
        } catch (IOException e) {
            throw new RuntimeException("Невозможно открыть серверный сокет");
        }
        while (true) {
            try (
                    Socket socket = Objects.requireNonNull(serverSocket).accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    OutputStream outputStream = socket.getOutputStream();
            ) {
                String request = readRequest(reader);
                createResponse(request, outputStream, Paths.get(properties.getProperty("directory")), properties.getProperty("fileExtension"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private ApplicationProperties getProperties() {
        ApplicationProperties properties = new ApplicationProperties(new Properties());
        try {
            properties.load(new FileInputStream(pathToProperties));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private int getFreeId(Path path, String fileExtension) {
        Set<Integer> ids = getFileList(path, fileExtension)
                .stream()
                .map(v -> Integer.valueOf(v.getName().substring(0, v.getName().length() - fileExtension.length())))
                .collect(Collectors.toSet());
        for (int i = 0; true; i++) {
            if (!ids.contains(i)) {
                return i;
            }
        }
    }

    private Set<File> getFileList(Path path, String fileExtension) {
        return Arrays
                .stream(Objects.requireNonNull(path.toFile().listFiles()))
                .filter(v -> v.getName().endsWith(fileExtension)).collect(Collectors.toSet());
    }

    private void createResponse(String query, OutputStream outputStream, Path path, String fileExtension) {
        JSONFormatter jsonFormatter = new JSONFormatterImpl();
        Response response = new Response(outputStream, jsonFormatter);
        try {
            if (query.startsWith("user")) {
                if (query.contains("create")) {
                    Map<String, String> args = Arrays.stream(query.substring(query.indexOf('?') + 1).split("&"))
                            .collect(Collectors.toMap(v -> v.split("=")[0], v -> v.split("=")[1]));
                    int id = saveUser(new User(args.get("name"), Integer.parseInt(args.get("age")), Integer.parseInt(args.get("salary"))), path, fileExtension);
                    response.sendResponse("ID " + id);
                } else if (query.contains("delete")) {
                    int id = Integer.parseInt(query.substring(query.lastIndexOf('/') + 1, query.length()));
                    response.sendResponse(deleteUser(id, path, fileExtension));
                } else if (query.contains("list")) {
                    response.sendResponse(getUserList(path, fileExtension));
                } else {
                    int id = Integer.parseInt(query.substring(query.lastIndexOf('/') + 1, query.length()));
                    response.sendResponse(getUser(path, id, fileExtension));
                }
            } else {
                response.sendResponse(null);
            }
        } catch (Exception e) {
            response.sendResponse(e.toString());
            e.printStackTrace();
        }
    }

    private int saveUser(User user, Path path, String fileExtension) {
        int id = getFreeId(path, fileExtension);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toString() + File.separator + id + fileExtension))) {
            out.writeObject(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
    }

    private boolean deleteUser(int id, Path path, String fileExtension) {
        File file = new File(path.toString() + File.separator + id + fileExtension);
        return file.delete();
    }

    private List<User> getUserList(Path path, String fileExtension) {
        Set<File> fileSet = getFileList(path, fileExtension);
        List<User> users = new ArrayList<>();
        for (File file : fileSet) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                users.add((User) in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    private User getUser(Path path, int id, String fileExtension) {
        File file = new File(path.toString() + File.separator + id + fileExtension);
        if (file.isFile()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                return ((User) in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
