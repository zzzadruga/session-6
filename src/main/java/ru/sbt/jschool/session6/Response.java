package ru.sbt.jschool.session6;

import ru.sbt.jschool.session6.JSONFormatter.JSONFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Response {
    private static final String DONE = "Done";
    private static final String FORMAT = "HTTP/1.1 %s\r\n" +
            "Date: %s\r\nServer: java/1.8\r\nContent-Type: %s\r\nContent-Length: %s\r\nConnection: keep-alive\r\n\n%s";
    private BufferedWriter out;
    private JSONFormatter jsonFormatter;

    public Response(OutputStream outputStream, JSONFormatter jsonFormatter) {
        this.out = new BufferedWriter(new OutputStreamWriter(outputStream));
        this.jsonFormatter = jsonFormatter;
    }

    public void sendResponse(Object responseValue) {
        try {
            out.write(createResponse(responseValue));
            out.flush();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private String createResponse(Object responseValue) throws IllegalAccessException {
        if (responseValue == null) {
            return String.format(FORMAT, Status.NOT_FOUND, getServerTime(), ContentType.HTML, Status.NOT_FOUND.toString().length(), Status.NOT_FOUND);
        } else if (responseValue instanceof String) {
            return String.format(FORMAT, Status.OK, getServerTime(), ContentType.HTML, ((String) responseValue).length(), responseValue);
        } else if (responseValue instanceof Boolean){
            if ((Boolean)responseValue){
                return String.format(FORMAT, Status.OK, getServerTime(), ContentType.HTML, DONE.length(), DONE);
            } else {
                return String.format(FORMAT, Status.NOT_FOUND, getServerTime(), ContentType.HTML, Status.NOT_FOUND.toString().length(), Status.NOT_FOUND);
            }
        } else {
            String stringValue = jsonFormatter.marshall(responseValue);
            return String.format(FORMAT, Status.OK, getServerTime(), ContentType.JSON, stringValue.length(), stringValue);
        }
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        return dateFormat.format(calendar.getTime());
    }
}
