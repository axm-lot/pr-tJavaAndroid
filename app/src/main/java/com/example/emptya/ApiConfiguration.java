package com.example.emptya;

public class ApiConfiguration {
    private static final String IP_ADDRESS = "192.168.1.114";
    private static final int PORT = 3000;
    private static final String API_PATH = "/api/";

    public static String getApiUrl() {
        return "http://" + IP_ADDRESS + ":" + PORT + API_PATH;
    }
}
