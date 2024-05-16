package io.hydrocarbon.rush.java.network;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zou Zhenfeng
 * @since 2024-05-16
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        // 设置服务器端口号
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("WebSocket 服务器已启动，监听端口 {}...", port);

            // 接受客户端连接
            Socket clientSocket = serverSocket.accept();

            // 处理连接
            Thread.ofVirtual().start(() ->
                    handleConnection(clientSocket)
            );

        } catch (IOException e) {
            log.error("服务器启动失败: {}", e.getMessage(), e);
        }
    }

    private static void handleConnection(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream();
             Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);

             BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
        ) {
            String data = scanner.useDelimiter("\r\n\r\n").next();
            Matcher matcher = Pattern.compile("^GET").matcher(data);

            if (!matcher.find()) {
                log.error("WebSocket 握手失败: 不是 WebSocket 握手请求");
                return;
            }

            Matcher secKeyMatcher = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            if (!secKeyMatcher.find()) {
                log.error("WebSocket 握手失败: Sec-WebSocket-Key 未找到");
                return;
            }

            String response = """
                    HTTP/1.1 101 Switching Protocols
                    Connection: Upgrade
                    Upgrade: websocket
                    Sec-WebSocket-Accept: %s
                    """;

            String secKey = secKeyMatcher.group(1);
            response = String.format(response, getSecAccept(secKey));
            out.write(response);
            out.newLine();
            out.flush();

            log.info("WebSocket 握手成功");

            // TODO 解析数据，这里需要解密。因为是基于 TCP 的，这里需要自己实现数据帧的解析
            //  by Zou Zhenfeng at 2024-05-16 16:54
        } catch (IOException e) {
            log.error("处理连接时出错: {}", e.getMessage(), e);
        } finally {
            log.info("关闭连接中...");
            try {
                clientSocket.close();
                log.info("连接已关闭");
            } catch (IOException e) {
                log.error("关闭连接时出错: {}", e.getMessage(), e);
            }
        }
    }

    private static String getSecAccept(String secKey) {
        String secAccept;
        try {
            secAccept = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-1")
                            .digest(
                                    (secKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                            .getBytes(StandardCharsets.UTF_8)
                            )
            );
        } catch (NoSuchAlgorithmException e) {
            log.error("加密算法不支持: {}", e.getMessage(), e);
            return null;
        }
        return secAccept;
    }
}
