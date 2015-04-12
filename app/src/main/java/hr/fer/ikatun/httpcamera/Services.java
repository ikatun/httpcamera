package hr.fer.ikatun.httpcamera;

import android.hardware.Camera;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by ikatun on 15.1.2015..
 */
public class Services {
    private static Camera camera = null;

    public static Camera getCameraInstance() {
        try {
            camera = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e) {
            Log.d("ikatun", e.toString());
            // Camera is not available (in use or does not exist)
        }
        return camera; // returns null if camera is unavailable
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[10 * 1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static List<String> readHttpRequest(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>();

        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            lines.add(line);
        }

        return lines;
    }

    public static InputStream getHttpResponsePrefix(int chunkIndex) {
        String prefix =
                "HTTP/1.1 200 OK\n" +
                "Date: Tue, 13 Jan 2015 21:17:48 GMT\n" +
                "Server: HttpCamera Web Server v 1\n" +
                "Accept-Ranges: bytes\n" +
//                "Content-Length: 0\n" +
                "Keep-Alive: timeout=5, max=100\n" +
                "Connection: Keep-Alive\n" +
                "Content-Type: video/mp4\n" +
                "Set-Cookie: chunkIndex="+chunkIndex+"\n\n";
        return new ByteArrayInputStream(prefix.getBytes(Charset.forName("UTF8")));
    }

    public static InputStream getHttpIndexResponse() {
        String index =
                "HTTP/1.1 200 OK\n" +
                "Content-Type: text/html; charset=utf-8\n" +
                "Set-Cookie: chunkIndex=0\n"+
                "\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<title>Http Camera Stream</title>\n" +
                "\t\t<script type=\"text/javascript\">\n" +
                "\t\t\tfunction play() {\n" +
                "\t\t\t\t\n" +
                "\t\t\t}\n" +
                "\t\t</script>\n" +
                "\t</head>\n" +
                "\t<body style=\"background-color: black;\">\n" +
                "\t\t<video width=\"800\" height=\"600\" id=\"video\" oncanplay=\"this.play()\" onended=\"this.load()\" >\n" +
                "\t\t  <source src=\"http://192.168.0.17:12345/stream.mp4\" type=\"video/mp4\">\n" +
                "\t\tYour browser does not support the video tag.\n" +
                "\t\t</video>\n" +
                "\t</body>\n" +
                "</html>";
        return new ByteArrayInputStream(index.getBytes(Charset.forName("UTF8")));
    }

    public static List<String> getIPAddresses() {
        List<String> list = new ArrayList<String>();
        Enumeration e = null;
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String str = i.getHostAddress();
                    if (Character.isDigit(str.charAt(0)) && !str.equals("127.0.0.1")) {
                        list.add(i.getHostAddress());
                    }
                }
            }
        } finally {
            return list;
        }
    }

    public static String getPath(List<String> requestHeader) {
        for (String line : requestHeader) {
            String[] words = line.split(" ");
            if (words.length > 1 && words[0].toLowerCase().equals("get")) {
                return words[1].substring(1);
            }
        }

        return "";
    }

    public static int getLastChunkCount(List<String> requestHeader) {
        for (String line : requestHeader) {
            String[] words = line.replace(";", "").split(" ");
            if (words.length > 1 && words[0].toLowerCase().equals("cookie:")) {
                return Integer.parseInt(words[1].substring("chunkIndex=".length()));
            }
        }
        return 0;
    }

    public static InputStream getHttpNotFoundResponse() {
        return new ByteArrayInputStream("HTTP/1.1 404 Not Found\n\n".getBytes(Charset.forName("UTF8")));
    }
}
