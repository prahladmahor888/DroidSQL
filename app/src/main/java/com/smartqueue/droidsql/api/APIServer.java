package com.smartqueue.droidsql.api;

import com.smartqueue.droidsql.model.DatabaseManager;
import com.smartqueue.droidsql.model.QueryResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lightweight local REST API server running inside DroidSQL using native Java Sockets.
 * Avoids compile-time JDK dependency issues under Android compilation environments.
 */
public class APIServer {
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private DatabaseManager databaseManager;
    private String authToken;
    private boolean isRunning = false;
    private ExecutorService threadPool;
    private Thread serverThread;

    public APIServer(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.authToken = generateToken();
    }

    public APIServer(DatabaseManager databaseManager, String authToken) {
        this.databaseManager = databaseManager;
        this.authToken = authToken;
    }

    /**
     * Starts the HTTP Server on a background thread.
     */
    public synchronized void start() {
        if (isRunning) {
            return;
        }
        if (this.authToken == null || this.authToken.isEmpty()) {
            this.authToken = generateToken();
        }
        isRunning = true;
        threadPool = Executors.newCachedThreadPool();
        
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        if (!isRunning) {
                            if (clientSocket != null) {
                                clientSocket.close();
                            }
                            break;
                        }
                        threadPool.execute(() -> handleClient(clientSocket));
                    } catch (IOException e) {
                        if (!isRunning) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });
        serverThread.start();
    }

    /**
     * Stops the HTTP Server and releases all socket and thread resources.
     */
    public synchronized void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
        serverSocket = null;
        threadPool = null;
        serverThread = null;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getPort() {
        return PORT;
    }

    private String generateToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void handleClient(Socket socket) {
        try (InputStream input = socket.getInputStream();
             OutputStream output = socket.getOutputStream()) {
             
            // Read headers byte-by-byte until double CRLF (\r\n\r\n) is found
            ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
            int b;
            int consecutiveLf = 0;
            while ((b = input.read()) != -1) {
                headerBuffer.write(b);
                if (b == '\n') {
                    consecutiveLf++;
                    if (consecutiveLf == 2) {
                        break;
                    }
                } else if (b != '\r') {
                    consecutiveLf = 0;
                }
            }
            
            byte[] headerBytes = headerBuffer.toByteArray();
            if (headerBytes.length == 0) {
                return;
            }
            
            String headerString = new String(headerBytes, StandardCharsets.UTF_8);
            String[] lines = headerString.split("\r\n");
            if (lines.length == 0 || lines[0].isEmpty()) {
                return;
            }
            
            String requestLine = lines[0];
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendRawResponse(output, 400, "Bad Request", "{\"error\":\"Bad Request\"}");
                return;
            }
            
            String method = requestParts[0];
            String fullPath = requestParts[1];
            
            // Parse headers
            Map<String, String> headers = new HashMap<>();
            int contentLength = 0;
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                int colonIdx = line.indexOf(':');
                if (colonIdx != -1) {
                    String key = line.substring(0, colonIdx).trim().toLowerCase();
                    String val = line.substring(colonIdx + 1).trim();
                    headers.put(key, val);
                    if ("content-length".equals(key)) {
                        try {
                            contentLength = Integer.parseInt(val);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            // Read request body
            String body = "";
            if (contentLength > 0) {
                byte[] bodyBytes = new byte[contentLength];
                int totalRead = 0;
                while (totalRead < contentLength) {
                    int read = input.read(bodyBytes, totalRead, contentLength - totalRead);
                    if (read == -1) {
                        break;
                    }
                    totalRead += read;
                }
                body = new String(bodyBytes, StandardCharsets.UTF_8);
            }

            String path = fullPath;
            String query = "";
            int questionIdx = fullPath.indexOf('?');
            if (questionIdx != -1) {
                path = fullPath.substring(0, questionIdx);
                query = fullPath.substring(questionIdx + 1);
            }
            
            Map<String, String> params = parseQueryParams(query);

            // Handle OPTIONS preflight CORS requests
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendOptionsResponse(output);
                return;
            }

            try {
                // Authenticate token
                String apiKeyHeader = headers.get("x-api-key");
                String apiKeyParam = params.get("token");
                boolean authorized = (apiKeyHeader != null && apiKeyHeader.equals(authToken)) ||
                                     (apiKeyParam != null && apiKeyParam.equals(authToken));

                if (!authorized) {
                    JSONObject errorJson = new JSONObject();
                    errorJson.put("error", "Unauthorized");
                    errorJson.put("message", "Invalid or missing token in header (X-API-Key) or query parameter (token)");
                    sendRawResponse(output, 401, "Unauthorized", errorJson.toString());
                    return;
                }

                if ("/status".equals(path)) {
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("status", "running");
                    String dbName = databaseManager.getCurrentDatabaseName();
                    responseJson.put("database", dbName != null ? dbName : "None");
                    responseJson.put("is_database_open", databaseManager.isDatabaseOpen());
                    sendRawResponse(output, 200, "OK", responseJson.toString());
                    return;
                }

                if ("/query".equals(path)) {
                    String sql = "";
                    if ("GET".equalsIgnoreCase(method)) {
                        sql = params.get("sql");
                    } else if ("POST".equalsIgnoreCase(method)) {
                        String contentType = headers.get("content-type");
                        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
                            if (!body.trim().isEmpty()) {
                                try {
                                    JSONObject json = new JSONObject(body);
                                    sql = json.optString("sql", "");
                                } catch (Exception e) {
                                    JSONObject errorJson = new JSONObject();
                                    errorJson.put("success", false);
                                    errorJson.put("error", "Invalid JSON body: " + e.getMessage());
                                    sendRawResponse(output, 400, "Bad Request", errorJson.toString());
                                    return;
                                }
                            }
                        } else {
                            // Fallback to URL-encoded parameters or raw body
                            Map<String, String> bodyParams = parseQueryParams(body);
                            sql = bodyParams.get("sql");
                            if (sql == null || sql.isEmpty()) {
                                sql = body; // Treat whole body as raw SQL
                            }
                        }
                    } else {
                        sendRawResponse(output, 405, "Method Not Allowed", "{\"error\":\"Method Not Allowed\"}");
                        return;
                    }

                    if (sql == null || sql.trim().isEmpty()) {
                        JSONObject errorJson = new JSONObject();
                        errorJson.put("success", false);
                        errorJson.put("error", "Missing parameter: sql");
                        sendRawResponse(output, 400, "Bad Request", errorJson.toString());
                        return;
                    }

                    try {
                        QueryResult result = databaseManager.executeSQL(sql);
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("success", result.isSuccess());
                        responseJson.put("execution_time_ms", result.getExecutionTimeMs());
                        
                        if (result.isSuccess()) {
                            responseJson.put("message", result.getMessage());
                            responseJson.put("rows_affected", result.getRowsAffected());
                            
                            if (result.hasColumns() && result.hasRows()) {
                                JSONArray colsArray = new JSONArray(result.getColumnNames());
                                responseJson.put("columns", colsArray);
                                
                                JSONArray rowsArray = new JSONArray();
                                for (List<String> row : result.getRows()) {
                                    JSONArray rowArray = new JSONArray(row);
                                    rowsArray.put(rowArray);
                                }
                                responseJson.put("rows", rowsArray);
                            }
                        } else {
                            responseJson.put("error", result.getMessage());
                        }
                        
                        sendRawResponse(output, 200, "OK", responseJson.toString());
                    } catch (Exception e) {
                        JSONObject errorJson = new JSONObject();
                        errorJson.put("success", false);
                        errorJson.put("error", "Internal Server Error: " + e.getMessage());
                        sendRawResponse(output, 500, "Internal Server Error", errorJson.toString());
                    }
                    return;
                }

                // Path not found
                sendRawResponse(output, 404, "Not Found", "{\"error\":\"Not Found\"}");
            } catch (org.json.JSONException e) {
                sendRawResponse(output, 400, "Bad Request", "{\"success\":false,\"error\":\"JSON error: " + e.getMessage() + "\"}");
            } catch (Exception e) {
                sendRawResponse(output, 500, "Internal Server Error", "{\"success\":false,\"error\":\"Server error: " + e.getMessage() + "\"}");
            }

        } catch (IOException e) {
            // Socket errors are ignored to prevent crash
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                String value = (idx > 0 && pair.length() > idx + 1)
                        ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                        : "";
                params.put(key, value);
            } catch (Exception e) {
                // Ignore decoding errors
            }
        }
        return params;
    }

    private void sendRawResponse(OutputStream output, int statusCode, String statusText, String responseBody) throws IOException {
        byte[] bodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        header.append("Content-Type: application/json; charset=utf-8\r\n");
        header.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        header.append("Access-Control-Allow-Origin: *\r\n");
        header.append("Access-Control-Allow-Headers: Content-Type, X-API-Key, Authorization\r\n");
        header.append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n");
        header.append("Connection: close\r\n");
        header.append("\r\n");
        
        output.write(header.toString().getBytes(StandardCharsets.UTF_8));
        output.write(bodyBytes);
        output.flush();
    }

    private void sendOptionsResponse(OutputStream output) throws IOException {
        StringBuilder header = new StringBuilder();
        header.append("HTTP/1.1 204 No Content\r\n");
        header.append("Access-Control-Allow-Origin: *\r\n");
        header.append("Access-Control-Allow-Headers: Content-Type, X-API-Key, Authorization\r\n");
        header.append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n");
        header.append("Connection: close\r\n");
        header.append("\r\n");
        
        output.write(header.toString().getBytes(StandardCharsets.UTF_8));
        output.flush();
    }
}
