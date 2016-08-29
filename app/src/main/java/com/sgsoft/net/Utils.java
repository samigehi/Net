package com.sgsoft.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.JsonObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utils {

	private static String BASE_URL;

	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			return false;
		} else
			return true;
	}

	public static String get(JsonObject object, String key) {
		if (object != null && object.has(key))
			if (!object.get(key).isJsonNull())
				return object.get(key).getAsString();
		return "";
	}

	public static boolean getBool(JsonObject object, String key) {
		if (object != null && object.has(key))
			if (!object.get(key).isJsonNull())
				return Boolean.parseBoolean(object.get(key).getAsString());
		return false;
	}

	public static int getInt(JsonObject object, String key) {
		if (object != null && object.has(key))
			if (!object.get(key).isJsonNull()) {
				try {
					if (object.get(key).getAsJsonPrimitive().isNumber())
						return object.get(key).getAsInt();
					return Integer.parseInt(object.get(key).getAsString());
				} catch (NumberFormatException e) {
					loge("getInt error occured", e);
				}
			}
		return -1;
	}

	public static Double getDouble(JsonObject object, String key) {
		if (object != null && object.has(key))
			if (!object.get(key).isJsonNull()) {
				try {
					return object.get(key).getAsDouble();
				} catch (Exception e) {
					loge("getDouble An error occured", e);
					try {
						return Double.parseDouble(object.get(key).getAsString());
					} catch (Exception e2) {
					}
				}
			}
		return -1.00;
	}

	public static String getBaseUrl() {
		return BASE_URL;
	}

	public static void setBaseUrl(String url) {

		BASE_URL = url;
	}

	public static String createUrl(String url) {
		if (url.startsWith("/"))
			url = url.substring(1);
		return BASE_URL + url;
	}

	public static String createUrl(String... urls) {
		String url = "";
		for (int i = 0; i < urls.length; i++) {
			url += urls[i];
		}
		return BASE_URL + url;
	}

	public static void loge(String msg, Throwable e) {
		Log.e("Net", msg, e);
	}

	public static void loge(String tag, String msg) {
		Log.e(tag, msg);
	}

	public static void log(String tag, String msg) {
		Log.i(tag, msg);
	}

	public static SSLSocketFactory trustAll() throws NoSuchAlgorithmException, CertificateException, IOException,
			KeyStoreException, KeyManagementException {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		return new MySSLSocketFactory(trustStore);

	}

	public static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
			// super(truststore);
			super();

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return null;
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return null;
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return null;
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
				throws IOException, UnknownHostException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
			return sslContext.getSocketFactory().createSocket(address, port, address, localPort);
		}

		@Override
		public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
			return sslContext.getSocketFactory().createSocket(s, host, port, autoClose);
		}
	}
}
