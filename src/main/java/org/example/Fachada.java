package org.example;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Fachada {

    private static RedirBack redirBack = new RedirBack();
    public static void main(String[] args) throws Exception {
        start();
    }

    public static void start() throws Exception {
        int port = 8080;
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
        String full = in.readLine();
        String[] parts = full.split(" ");
        System.out.println(parts[1]);
        if(!parts[1].contains("/getkv?key") && !parts[1].contains("/setkv?key=")
                && !parts[1].contains("cliente")){
            notFound(out);
        }


        if(parts[1].contains("/getkv?key") || parts[1].contains("/setkv?key=")){
            String response = redirBack.get(parts[1]);
            ok(out,response);
        }

        if(parts[1].contains("/cliente")){
            getCliente(out);
        }
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

    private static void ok(PrintWriter out,String jsonObject){
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonObject.length());
        out.println();
        out.println(jsonObject);
        out.close();
    }

    private static void getCliente(PrintWriter out){
        String cliente = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>cliente</title>
                </head>
                <body>
                    <h1>peticion set</h1>
                    <h2> key </h2>
                    <input id="key" type="text">
                    <h2> value </h2>
                    <input id="value" type="text">
                    <button onclick="sendSet()"> enviar </button>
                    <div id="out"></div>
                                
                    <h1>peticion get</h1>
                    <h2> key </h2>
                    <input id="key" type="text">
                    <button onclick="sendGet()"> enviar </button>
                    <div id="out2"></div>
                                
                    <script>
                        function sendSet(){
                            const key = document.getElementById("key").value;
                            const value = document.getElementById("value").value;
                                
                            const path = "/setkv?key=" + key + "&value=" + value\s
                            fetch(path)
                                .then(data => data.text())
                                .then(data => document.getElementById("out").textContent = data)
                                .catch(error => document.getElementById("out").textContent = error)
                        }
                                
                        function sendGet(){
                            const key = document.getElementById("key").value;
                       \s
                                
                            const path = "/getkv?key=" + key\s
                            fetch(path)
                                .then(data => data.text())
                                .then(data => document.getElementById("out2").textContent = data)
                                .catch(error => document.getElementById("out2").textContent = error)
                        }
                    </script>
                                
                </body>
                </html>
                """;

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println("Content-Length: " + cliente.length());
        out.println();
        out.println(cliente);
        out.close();
    }

}
