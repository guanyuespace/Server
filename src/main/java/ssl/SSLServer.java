package ssl;

import server.Server;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

//为其导入证书 -storepass 密码默认为changeit
//keytool -import -v -trustcacerts -alias fiddler -file D:\certificate\FiddlerRoot.cer -storepass changeit -keystore ..\jre\lib\security\cacerts
//keytool -import -v -trustcacerts -alias baidu -file D:\certificate\baidu-2018-0926.cer -storepass changeit -keystore ..\jre\lib\security\cacerts


//查看cacerts中的证书列表：
//              keytool -list -keystore "%JAVA_HOME%/jre/lib/security/cacerts"  -storepass changeit

//删除cacerts中指定名称的证书：
//                      keytool -delete -alias taobao -keystore "%JAVA_HOME%/jre/lib/security/cacerts"  -storepass changeit

//导入指定证书到cacerts：
//                      keytool -import -alias taobao -file taobao.cer -keystore "%JAVA_HOME%/jre/lib/security/cacerts"  -storepass changeit-trustcacerts
//生成证书
//                      keytool -genkeypair -keystore seckey -keyalg rsa -alias SSL
//                      keytool -genkeypair -v -alias "my client key" -validity 365 -keystore my.keystore
//                      keytool -genkeypair -v -alias root -keyalg RSA -storetype PKCS12 -keystore seckey -storepass changeit -keypass 1234567890
public class SSLServer implements Runnable {
    private SSLServerSocket sslServerSocket;

    private SSLServer() throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException, IOException {
        super();
        //准备KeyStore相关信息,使用keytool创建的证书：seckey
        String keyStoreName = "./seckey.store";////filepath
        char[] keyStorePwd = "changeit".toCharArray();//
        char[] keyPwd = "testhere".toCharArray();
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        //装载生成的seckey
        try (InputStream in = new FileInputStream(new File(keyStoreName))) {
            keyStore.load(in, keyStorePwd);
        } catch (IOException | CertificateException e) {
            e.printStackTrace();
        }

        //初始化KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPwd);

        //初始化SSLContext
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(kmf.getKeyManagers(), new TrustManager[]{getX509TrustManger()}, new SecureRandom());

        //监听和接受客户端连接
        SSLServerSocketFactory factory = context.getServerSocketFactory();
        sslServerSocket = (SSLServerSocket) factory.createServerSocket(443);
        System.out.println("服务器启动，绑定443端口");
    }

    public static void main(String[] args) throws UnrecoverableKeyException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        new Thread(new SSLServer()).start();
    }

    private static X509TrustManager getX509TrustManger() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    @Override
    public void run() {
        while (true) {
            //等待客户端连接
            Socket client;
            try {
                client = sslServerSocket.accept();
                System.out.println("客户端地址:" + client.getRemoteSocketAddress());
                new Thread(new Server.Listener(client.getInputStream(), client.getOutputStream(), client)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
