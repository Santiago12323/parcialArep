package org.example;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class httpServer {

    private static HashMap<String,String> map = new HashMap<>();
    public static void main(String[] args) throws Exception {
        start();
    }

    public static void start() throws Exception {
        int port = 8081;
        ServerSocket serverSocket = new ServerSocket(port);
        while (true){
            System.out.println("iniciando en servidor por puerto " + port);
            Socket client = serverSocket.accept();
            handleResponse(client);

        }
    }

    public static void handleResponse(Socket clientSocket) throws Exception {
        PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        try{
            String full = in.readLine();
            System.out.println(full);
            String[] parts = full.split(" ");
            if(!parts[1].contains("/getkv?key") && !parts[1].contains("/setkv?key=")){
                notFound(out);
            }
            String fullPath = parts[1];
            JsonObject jsonObject = getFuntion(fullPath);
            ok(out,jsonObject);
        }
        catch (Exception e){
            badRequest(out,e.getMessage());
        }
    }

    private static JsonObject getFuntion(String fullPath){
        String path = fullPath.split("[?=]")[0];
        try{
            if(path.contains("/getkv")){
                return getKey(fullPath);
            }
            if(path.contains("/setkv")){
                return setKey(fullPath);
            }
            else {
                new RuntimeException("path not found");
            }
        }
        catch (Exception e){
            new Exception(e);
        }
        return null;
    }

    private static JsonObject getKey(String fullPath){
        if(fullPath.split("[?=]").length < 3){
            new RuntimeException("not found, falta query");
        }
        String key = fullPath.split("[?=&]")[2];

        if(!map.containsKey(key)){
            new RuntimeException("key_not_found");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Error" , "key_not_found");
            jsonObject.addProperty("key" ,key);
            return jsonObject;
        }
        String value = map.get(key);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key" , key);
        jsonObject.addProperty("value" , value);
        return jsonObject;

    }

    private static JsonObject setKey(String fullPath){
        if(fullPath.split("[?=&]").length < 4){
            new RuntimeException("not found falta value o key");
        }
        String value = fullPath.split("[?=&]")[4];
        String key = fullPath.split("[?=&]")[2];
        try{
            map.put(key,value);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key" , key);
            jsonObject.addProperty("value" , value);
            jsonObject.addProperty("status" , "created");
            return jsonObject;
        }
        catch (Exception e){
            new Exception(e);
        }
        return null;
    }

    private static void badRequest(PrintWriter out, String response){
        out.println("HTTP/1.1 400 Bad Request");
        out.println("Content-Type: application/json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error" , response);
        out.println("Content-Length: " + jsonObject.toString().length());
        out.println();
        out.println(jsonObject.toString());
        out.close();
    }

    private static void notFound(PrintWriter out){
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: application/json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error" , "Not Found");
        out.println("Content-Length: " + jsonObject.toString().length());
        out.println();
        out.println(jsonObject.toString());
        out.close();
    }

    private static void ok(PrintWriter out,JsonObject jsonObject){
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonObject.toString().length());
        out.println();
        out.println(jsonObject.toString());
        out.close();
    }


}
