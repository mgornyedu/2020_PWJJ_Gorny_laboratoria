package Clients;

import Models.Project;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public abstract class BaseClient {
    protected static UUID ClientId = UUID.randomUUID();
    public BaseClient() throws IOException {
        InetAddress host = InetAddress.getLocalHost();
        socket = new Socket(host.getHostName(), 7702);
        RegisterClient();
    }


    Socket socket = null;

    public void UnregisterClient(){
        try {
            Request("UnregisterClient", ClientId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void RegisterClient() throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.println("RegisterClient");
        writer.println(ClientId.toString());
        writer.flush();
        BufferedReader brinp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        brinp.readLine();
    }
    protected  <T> ArrayList<T> ListRequest(final Class<T> dataClass, String operation, Object... parameters) throws IOException {
        Gson gson = new Gson();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.println(operation);
        writer.println(gson.toJson(parameters));
        writer.flush();
        BufferedReader brinp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = brinp.readLine();
        return gson.fromJson(line, getType(ArrayList.class, dataClass));
    }
    protected <T> T Request(final Class<T> dataClass, String operation, Object... parameters) throws IOException {
        Gson gson = new Gson();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.println(operation);
        writer.println(gson.toJson(parameters));
        writer.flush();
        BufferedReader brinp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = brinp.readLine();

        return gson.fromJson(line, dataClass);
    }
    protected void Request(String operation, Object... parameters) throws IOException {
        Gson gson = new Gson();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.println(operation);
        writer.println(gson.toJson(parameters));
        writer.flush();
        BufferedReader brinp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        brinp.readLine();
    }
    public void Close(){
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.println("close");
            writer.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    Type getType(Class<?> rawClass, Class<?> parameter) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {parameter};
            }
            @Override
            public Type getRawType() {
                return rawClass;
            }
            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
