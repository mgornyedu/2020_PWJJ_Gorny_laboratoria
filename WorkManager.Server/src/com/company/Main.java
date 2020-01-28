package com.company;

import DataAccess.MainRepository;
import Models.SyncType;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class Main {
    private static ServerSocket serverSocket;

    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
        new Server(7702).run();
    }

    public static class Server extends Thread{
        private int Port;

        public Server(int port) {
            Port = port;
        }

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(Port);
            } catch (IOException e) {
                e.printStackTrace();

            }
            while (true) {
                try {
                    socket = serverSocket.accept();
                    socket.setSoTimeout(1000000);
                    System.out.println("Klient na adresie: " + socket.getInetAddress());
                } catch (IOException e) {
                    System.out.println("I/O error: " + e);
                }
                // new thread for a client
                try {
                    new SocketServiceThread(socket, MainRepository.class).start();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        public static class SocketServiceThread<T> extends Thread {
            protected Socket socket;
            private Class<T> serviceType;
            private T service;

            public SocketServiceThread(Socket clientSocket, Class<T> serviceType) throws IllegalAccessException, InstantiationException {
                this.socket = clientSocket;
                this.serviceType = serviceType;
                service = serviceType.newInstance();
            }

            public void run() {
                InputStream inp = null;
                BufferedReader brinp = null;
                PrintStream out = null;
                try {
                    inp = socket.getInputStream();
                    brinp = new BufferedReader(new InputStreamReader(inp));
                    out = new PrintStream(socket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("I/O error: " + e);
                    return;
                }
                String operation, parameters;
                while (true) {
                    try {
                        operation = brinp.readLine();
                        parameters = brinp.readLine();
                        if(operation == null)
                            continue;
                        if (operation.equalsIgnoreCase("close")) {
                            socket.close();
                            return;
                        }
                        else if(operation.contains("RegisterClient")){
                            synchronized (SyncInformation.class) {
                                UUID id = UUID.fromString(parameters);
                                if (!SyncInformation.SyncData.containsKey(id)) {
                                    SyncInformation.SyncData.put(id, SyncType.None);
                                }
                                Method setClient = serviceType.getMethod("SetClient", UUID.class);
                                setClient.invoke(service, id);
                                out.println("");
                                out.flush();
                            }
                        }
                        else if(operation == "UnregisterClient"){
                            synchronized (SyncInformation.class){
                                UUID id = UUID.fromString(parameters);
                                if(SyncInformation.SyncData.containsKey(id)){
                                    SyncInformation.SyncData.remove(id);
                                }
                                out.println("");
                                out.flush();
                            }
                        }
                        else {
                            Gson gson = new Gson();
                            String finalOperation = operation;
                            Optional<Method> optional = Arrays.stream(serviceType.getMethods()).filter(x->x.getName().equalsIgnoreCase(finalOperation)).findFirst();
                            if(optional.isPresent()) {
                                Method method = optional.get();
                                JsonArray array = gson.fromJson(parameters, JsonArray.class);
                                Object[] resolvedArgs = new Object[method.getParameterCount()];
                                for(int i = 0; i< method.getParameterCount(); i++){
                                    resolvedArgs[i] = gson.fromJson(array.get(i), method.getParameterTypes()[i]);
                                }
                                String response = gson.toJson(method.invoke(service, resolvedArgs));
                                out.println(response);
                                out.flush();
                            }
                        }
                    }catch (SocketTimeoutException e){
                        try {

                            socket.close();
                            System.out.println("Socket timeout close: " + e);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        return;
                    } catch (IOException e) {
                        System.out.println("I/O error: " + e);
                        e.printStackTrace();
                        return;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}