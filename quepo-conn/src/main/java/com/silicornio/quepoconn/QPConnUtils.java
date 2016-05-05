package com.silicornio.quepoconn;

import android.content.Context;

import com.silicornio.quepoconn.general.QPL;
import com.silicornio.quepotranslator.QPCodeTranslation;
import com.silicornio.quepotranslator.QPCustomTranslation;
import com.silicornio.quepotranslator.QPTransConf;
import com.silicornio.quepotranslator.QPTransManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by SilicorniO
 */
public class QPConnUtils {

    /**
     * Generate a translator manager with custom translations and code translations
     * Custom translations: Calendar-Data
     * Code translations: Double-0
     * @return QPTransManager instance
     */
    public static QPTransManager generateTypicalTranslatorManager(QPTransConf conf){

        QPTransManager transManager = new QPTransManager(conf);
        transManager.setCheckTranslationsFirst(false);
        transManager.addCustomTranslation(new QPCustomTranslation<Calendar, Date>() {
            @Override
            public Date onTranslation(Calendar calendar) {
                return calendar.getTime();
            }

            @Override
            public Calendar onTranslationInverse(Date date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar;
            }
        });
        transManager.addCustomTranslation(new QPCustomTranslation<Double, String>(){
            @Override
            public String onTranslation(Double aDouble) {
                if(aDouble%1 == 0) {
                    return String.valueOf(aDouble.intValue());
                }else{
                    return String.valueOf(aDouble);
                }
            }

            @Override
            public Double onTranslationInverse(String s) {
                try {
                    return Double.parseDouble(s);
                }catch(NumberFormatException nfe){
                    return new Double(0d);
                }
            }
        });
        transManager.addCodeTranslation(new Double0QPCodeTranslation());

        return transManager;
    }

    private static class Double0QPCodeTranslation extends QPCodeTranslation<Double> {

        public Double0QPCodeTranslation(){
        }

        @Override
        public boolean match(Double d) {
            return d%1 == 0;
        }

        @Override
        public Object translate(Double d) {
            return Integer.valueOf(d.intValue());
        }
    }

    //----- HTTPS -----

    /**
     * Calling to this function we set a HostnameVerifier that always return true
     * accepting any URL although it is different to the certificate
     */
    public static void acceptDifferentHostnames(){

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        });
    }

    /**
     * Generate a SSLSocketFactory wich checks the certificate given
     * @param context Context to use
     * @param rResource int with url of the resource to read the certificate
     * @parma password String to use with certificate
     * @return SSLSocketFactory generated to validate this certificate
     */
    public static SSLSocketFactory newSslSocketFactory(Context context, int rResource, String password)
            throws CertificateException, NoSuchProviderException, KeyStoreException, NoSuchAlgorithmException,
            IOException, UnrecoverableKeyException, KeyManagementException{

        // Get an instance of the Bouncy Castle KeyStore format
        KeyStore trusted = KeyStore.getInstance("BKS");
        // Get the raw resource, which contains the keystore with
        // your trusted certificates (root and any intermediate certs)
        InputStream is = context.getApplicationContext().getResources().openRawResource(rResource);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509","BC");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(is);
        String alias = "alias";//cert.getSubjectX500Principal().getName();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
        kmf.init(trustStore, null);
        KeyManager[] keyManagers = kmf.getKeyManagers();

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(trustStore);
        TrustManager[] trustManagers = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext.getSocketFactory();

    }

    /**
     * Generate a SSLSocketFactory that accepts all certificates
     * @return SSLSocketFactory generated
     * @throws Exception
     */
    public static SSLSocketFactory createTrustAllSslSocketFactory(){
        try {

            TrustManager[] byPassTrustManagers = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

            } };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, byPassTrustManagers, new SecureRandom());
            return sslContext.getSocketFactory();
        }catch(Exception e){
            QPL.e("Exception creating all trust certificates factory: " + e.toString());
        }
        return null;
    }
}
