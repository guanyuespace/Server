package ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class SSLTest {
    public static void main(String[] params) throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager x509m = new X509TrustManager() {//管理X509证书，验证远程安全套接字
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        };
        // 获取一个SSLContext实例
        SSLContext s = SSLContext.getInstance("SSL");
        // 初始化SSLContext实例
        s.init(null, new TrustManager[]{x509m}, new java.security.SecureRandom());
        // 获取SSLContext实例相关的SSLEngine
        SSLEngine e = s.createSSLEngine();
        // 打印这个SSLContext实例使用的协议
        System.out.println("缺省安全套接字使用的协议: " + s.getProtocol());
        System.out.println("支持的协议: " + Arrays.asList(e.getSupportedProtocols()));
        System.out.println("启用的协议: " + Arrays.asList(e.getEnabledProtocols()));
        System.out.println("支持的加密套件: "
                + Arrays.asList(e.getSupportedCipherSuites()));
        System.out.println("启用的加密套件: "
                + Arrays.asList(e.getEnabledCipherSuites()));
    }
}
