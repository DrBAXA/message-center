package com.xmas.notifiers;

import com.xmas.exceptions.NotificationSendingException;
import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

@Service
public class MessageSanderBuilder {

    public ISender createSender(String urlString, Map<String, String> headers, String requestMethod) {
        try {
            URL url = new URL(urlString);
            Sender sender = new Sender((HttpURLConnection) url.openConnection());
            sender.setHeaders(headers);
            sender.setMethod(requestMethod);
            return sender;
        } catch (IOException ioe) {
            throw new NotificationSendingException(ioe.getMessage());
        }
    }


    private class Sender implements ISender {

        HttpURLConnection connection;

        @Override
        public void send(String JSONData) {
            try {
                connection.setInstanceFollowRedirects(true);
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(JSONData.getBytes());
                outputStream.flush();
                outputStream.close();

                if (connection.getResponseCode() > 300) {
                    throw new NotificationSendingException(connection.getResponseMessage());
                }
            }catch (IOException ioe){
                throw new NotificationSendingException(ioe.getMessage());
            }
        }

        public void setMethod(String method) throws ProtocolException {
            connection.setRequestMethod(method);
        }

        public void setHeaders(Map<String, String> headers) {
            headers.forEach(connection::setRequestProperty);
        }

        public Sender(HttpURLConnection connection) {
            this.connection = connection;
        }
    }
}
