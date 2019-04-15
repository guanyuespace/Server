package server;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server extends Thread {
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
//        serverSocket.setSoTimeout(10000);
    }

    public static void main(String[] args) {
        test();
//        try {
//            Thread t = new Server(80);
//            t.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static void test() {
        Pattern p = Pattern.compile("window.Gbanners =\n(\\[.*?]);", Pattern.DOTALL);
        String str = "window.Gbanners =\n[hello, what happened ? [bye];[yue];....];hello[yue];";
        p=Pattern.compile("window.Gbanners =\n(\\[[\\w\\d\\,\\?\\s]+(\\[\\w+\\];)+\\.+\\]);");
//        str="hello [yue1234[][]];";
//        p=Pattern.compile("(\\[[\\w\\d]+(\\[\\])+]);");
        Matcher matcher = p.matcher(str);
        if (matcher.find())
            System.out.println(matcher.group(1));
        else
            System.out.println("???");
    }


    public void run() {
        while (true) {
            try {
                System.out.println("等待远程连接，端口号为：" + serverSocket.getLocalPort() + "...");
                Socket client = serverSocket.accept();
                System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
                new Thread(new Listener(client.getInputStream(), client.getOutputStream(), client)).start();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static class Listener implements Runnable {
        private InputStream inputStream;
        private OutputStream outputStream;
        private Socket server;


        public Listener(InputStream inputStream, OutputStream outputStream, Socket server) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.server = server;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1024);
            try {
                String line;
                while (!(line = bufferedReader.readLine()).equals("")) {
                    if (line.startsWith("GET"))
                        break;
                }

                System.out.println(line);
                //轮播图
                Pattern patternPic = Pattern.compile("GET /discover\\s+.*");
                Matcher matcherPic = patternPic.matcher(line);

                //获取session数据
                Pattern pattern = Pattern.compile("GET /getSession\\?jscode=(.*)&appid=(.*)&appsecret=(.*)\\s+.*");
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) { //获取session数据
                    System.out.println("request session");
                    String jsCode = matcher.group(1);
                    String appid = matcher.group(2);
                    String appsecret = matcher.group(3);
                    String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid +
                            "&secret=" + appsecret + "&js_code=" + jsCode + "&grant_type=authorization_code";
                    String res = doGet(url);

                    if (outputStream != null) {
                        PrintWriter printWriter = new PrintWriter(outputStream, true);
                        printWriter.println("HTTP/1.1 200 OK");
                        printWriter.println("Content-Type:text/plain;charset=utf-8");
                        printWriter.println();
                        printWriter.println(res);
                    } else {
                        System.out.println("outputstream has been shutdown ...");
                    }
                } else if (matcherPic.matches()) {//获取轮播图
                    System.out.println("request pics from music163");
                    System.out.println("get pics from discover only by pc client");
                    String url = "https://music.163.com/discover";
                    String str = doGet(url);
                    String json = parseGetPics(str);

                    if (outputStream != null) {
                        PrintWriter printWriter = new PrintWriter(outputStream, true);
                        printWriter.println("HTTP/1.1 200 OK");
                        printWriter.println("Content-Type:text/plain;charset=utf-8");
                        printWriter.println();
                        printWriter.println(json == null ? "NULL" : json);
                    }
                } else {
                    System.out.println("???");
                }
                if (!server.isClosed())
                    server.close();
                System.out.println("end .....");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String parseGetPics(String str) {
            //In dotall mode, the expression <tt>.</tt> matches any character, including a line terminator.  By default this expression does not matchline terminators.
            Pattern pattern = Pattern.compile("window.Gbanners =[\n|\r\n](\\[.*?]);", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        private String doGet(String url) throws IOException {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream is = httpURLConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder(1024);
            byte[] bytes = new byte[512];
            int length;
            while ((length = is.read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, length));
            }
            httpURLConnection.disconnect();
            return stringBuilder.toString();
        }
    }
}
