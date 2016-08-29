package com.sgsoft.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

/**
 * Custom Java HTTP-Client for calling JSON SOAP/REST-Services
 * 
 * https://gist.github.com/Sumeet21/17af4711990fb8b5f001 //async helper class
 * for networking task created by
 * 
 * @return parameterized response e.g: JsonObject/JsonArray/image/bitmap/string
 *         etc
 * 
 * @author sumeet.kumar
 */
public class Net<A> extends AsyncTask<Object, Integer, A> {

	// http://stackoverflow.com/a/31455630/2685454 on rotate
	// http://www.codeproject.com/Articles/162201/Painless-AsyncTask-and-ProgressDialog-Usage

	// A is an object reference as u want to get response
	// methods
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String APP_JSON = "application/json";
	public static final String APP_TEXT = "text/plain";
	public static final String CONTENT_TYPE = "Content-Type";
	public final static String ISO_8859_1 = "ISO-8859-1";

	final boolean isLog = true;
	final String TAG = "HTTPS REQUEST";
	final String UTF_8 = "UTF-8";
	// final String ASCII = "UTF-8";
	final String APP_FORM = "application/x-www-form-urlencoded";
	final String TYPE_JSON = "?type=json";
	// A a;
	Callback<A> callback;
	private ProgressCallback progressCallback;
	private String id, url = "";
	private boolean isPost, asDrawable, asFormJson, asString;
	Activity context;
	int connectTimeout = 20000, readTimeout = 30000; // default 20 sec and 30
														// sec
														// respectively
	// String baseUri = API.getBaseUrl();
	private long time;
	HttpURLConnection connection;
	private JsonObject jsonObject;
	public boolean doInput = true, doOutput = true, useCache = false, isException;
	Exception exception;

	private Net() {

	}

	/**
	 * must use with local context not global context, when local context finish
	 * request will be cancelled otherwise it run in background until complete
	 * 
	 * @see Net.isCancel
	 */
	private Net(Activity c, String server) {
		context = c;
		url = server;
		exception = new Exception();
	}

	/** create new HTTP client for network communication */
	public static <C> Net<C> create(Activity c, String url) {
		return new Net<C>(c, url);
	}

	/** create new HTTP client with concatenate base URL from API class */
	public static <C> Net<C> with(Activity c, String url) {
		return new Net<C>(c, Utils.createUrl(url));
	}

	/** execute request and wait for response */
	public static <D> AsyncTask<Object, Integer, D> get(Activity c, String url, Callback<D> completeListner) {
		return new Net<D>(c, url).execute(completeListner);
	}

	// Object object;

	/**
	 * execute request on new thread in background and wait for response, Method
	 * POST/GET, return reference of current task
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public AsyncTask<Object, Integer, A> execute(Callback<A> completeListner) {
		setOnCallbackListner(completeListner);
		if (url == null)
			return null;
		if (isCancel())
			return null;
		// object = new A();
		// checkType(new A());
		if (Utils.isConnected(context)) {
			try {
				// throws rejected pool exception if more than 128 request
				// executed at a time, we know our app not exceed the limit, but
				// its better to use catch
				return executeOnExecutor(THREAD_POOL_EXECUTOR, "");
				//return this;
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
				return execute(SERIAL_EXECUTOR, "");
				//return this;
			}
		} else {
			if (completeListner != null) {
				completeListner.onComplete(null, new TimeoutException());
			}
			return null;
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// lockScreen(context);
		time = System.currentTimeMillis();
		isCancel();
	}

	// TODO do inBackground start
	// execute request in background on new thread
	@SuppressWarnings("unchecked")
	@Override
	protected A doInBackground(Object... params) {
		loge("strat " + (time - System.currentTimeMillis()) + " ms");
		loge("URL " + url);
		// JsonArray array = null;
		try {
			URL Url = new URL(url);
			// URLConnection urlConnection = Url.openConnection();
			if (url.toLowerCase().startsWith("https://")) {
				HttpsURLConnection.setDefaultHostnameVerifier(new HostVerifier());
				HttpsURLConnection.setDefaultSSLSocketFactory(Utils.trustAll());
				connection = (HttpsURLConnection) Url.openConnection();
			} else
				connection = (HttpURLConnection) Url.openConnection();

			connection.setConnectTimeout(getConnectTimeout());
			connection.setReadTimeout(getReadTimeout());
			connection.setUseCaches(useCache);
			// urlConnection.connect();
			// bypass(urlConnection);
			if (isPost()) {
				log("connection is post");
				connection.setRequestMethod(POST);
				connection.setRequestProperty("charset", UTF_8);
				connection.setRequestProperty(CONTENT_TYPE, APP_JSON);
				String body = jsonObject.toString();
				if (body != null)
					connection.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
				// connection.setFixedLengthStreamingMode(body.getBytes().length);

				connection.setDoInput(doInput);
				connection.setDoOutput(doOutput);
				if (body != null && body.length() > 0) {
					log("POST DATA " + body);
					OutputStream os = connection.getOutputStream();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF_8));
					writer.write(body);
					writer.flush();
					writer.close();
				}
				// os.close();
			} else
				connection.setRequestProperty(CONTENT_TYPE, APP_TEXT);

			if (isCancel())
				return null;

			int response = connection.getResponseCode();
			log("response code " + response);
			if (200 <= response && response <= 399) {
				// InputStream is = connection.getInputStream();
				if (isDrawable())
					return (A) drawable(connection.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()), 2048);
				String line;
				StringBuilder result = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
				reader.close();
				if (isString())
					return (A) result.toString();
				try {
					return (A) parse(result);
				} catch (Exception e) {
					e.printStackTrace();
					loge(e.getLocalizedMessage());
					exception = e;
					return null;
				}
				// return (A) array;
			}
			exception = new SocketTimeoutException();
			return null;

		} catch (Exception e1) {
			e1.printStackTrace();
			loge("ERROR:" + e1.getLocalizedMessage());
			log("ERROR:" + e1.toString());

			exception = e1;
			return null;
		}
	}

	@Override
	protected void onPostExecute(A result) {
		super.onPostExecute(result);
		// unlockScreen(context);
		loge("stop " + (time - System.currentTimeMillis()) + " ms");
		if (isCancel())
			return;
		try {
			if (result != null)
				setComplete(result, null);
			else
				setComplete(null, exception);
		} catch (Exception e1) {
			// e1.printStackTrace();
			if (e1 instanceof ClassCastException)
				exception = new ClassCastException();
			else
				exception = e1;
			loge("result:" + e1.getLocalizedMessage());
			setComplete(null, exception);
		}

		try {
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// unlockScreen(context);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (!isCancel()) {
			if (progressCallback != null)
				progressCallback.onProgress(values[0]);
		}
	}

	/// check context is alive/exist otherwise cancel the request to prevent
	/// memory leak issue
	private boolean isCancel() {
		if (context == null || isCancelled() || context.isFinishing()) {
			super.cancel(true);
			loge("dead context request is cancelled");
			return true;
		}
		return false;
	}

	// close connection
	private void close() throws IOException {
		if (connection != null) {
			if (connection.getInputStream() != null)
				connection.getInputStream().close();
			connection.disconnect();
		}
	}

	private void setComplete(A result, Exception ex) {
		if (callback != null)
			callback.onComplete(result, ex);
	}

	// @Test
	public A getA() throws ClassNotFoundException {
		// if(inferedClass == null){
		Type mySuperclass = getClass().getGenericSuperclass();
		Type tType = ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
		String className = tType.toString().split(" ")[1];
		return (A) Class.forName(className);
		// }
	}

	// convert string to gson JsonArray
	private JsonArray parse(StringBuilder object) {
		try {
			if (object == null) {
				JsonArray array = new JsonArray();
				JsonObject o = new JsonObject();
				o.addProperty("RESULT", "OUTPUT IS NULL");
				array.add(o);
				return array;
			}
			// log("length "+object.length());
			JsonElement element;
			if (!object.toString().startsWith("[")) {
				return new JsonParser().parse("[" + object.toString() + "]").getAsJsonArray();
			}
			element = new JsonParser().parse(object.toString());
			JsonArray array = element.getAsJsonArray();
			Utils.log(TAG, array.toString());
			return array;
		} catch (Exception e) {
			e.printStackTrace();
			JsonArray array = new JsonArray();
			JsonObject o = new JsonObject();
			o.addProperty("RESULT", "OUTPUT IS NULL");
			array.add(o);
			return array;
		}
	}

	/** add body parameter to send via post request, in JSON Format */
	public Net<A> add(String key, String value) {
		if (TextUtils.isEmpty(key))
			return this;
		if (value == null)
			value = "";
		if (jsonObject == null) {
			jsonObject = new JsonObject();
			asPost();
		}
		jsonObject.addProperty(key, value);
		return this;
	}

	public void addParameters(String key, String value) {
		add(key, value);
	}

	public void setOnCallbackListner(Callback<A> listner) {
		callback = listner;
	}

	public boolean isPost() {
		return isPost;
	}

	public void asPost() {
		isPost = true;
	}

	public Net<A> setPost(boolean isPost) {
		this.isPost = isPost;
		return this;
	}

	public Drawable drawable(InputStream is) {
		return new BitmapDrawable(null, BitmapFactory.decodeStream(is));
	}

	public boolean isDrawable() {
		return asDrawable;
	}

	/** load request as drawabale or bitmap image */
	public void asDrawable() {
		asDrawable(true);
	}

	/** load request as drawabale or bitmap image */
	public void asDrawable(boolean asDrawable) {
		this.asDrawable = asDrawable;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	/** set timeout for connect to network */
	public void setConnectTimeout(int timeOut) {
		this.connectTimeout = timeOut;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	/** set timeout for read connection */
	public void setReadTimeout(int timeOut) {
		this.readTimeout = timeOut;
	}

	private void log(String msg) {
		if (msg == null)
			return;
		if (isLog)
			Utils.log(TAG, msg);
	}

	private void loge(String msg) {
		if (msg == null)
			return;
		Utils.loge(TAG, msg);
	}

	public boolean isAsFormJson() {
		return asFormJson;
	}

	public void asAsFormJson() {
		asFormJson = true;
	}

	public boolean isString() {
		return asString;
	}

	public void asString() {
		setAsString(true);
	}

	public void setAsString(boolean asString) {
		this.asString = asString;
	}

	/**
	 * get unique id associated with this request
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public A getResult() throws InterruptedException, ExecutionException {
		return (A) super.get();
	}

	public Callback<A> getCallback() {
		return callback;
	}

	// lock screen orientation when task is running to prevent loader and memory
	// leak issue
	public void lockScreen(Activity c) {
		int current = c.getResources().getConfiguration().orientation;
		if (current == Configuration.ORIENTATION_PORTRAIT) {
			c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			c.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	public void unlockScreen(Activity a) {
		a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	public ProgressCallback getProgressCallback() {
		return progressCallback;
	}

	public void setProgressCallback(ProgressCallback callback) {
		this.progressCallback = callback;
	}

	public class HostVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			Utils.log("HOST NAME ", hostname);
			if (hostname.contentEquals("127.0.0.1")) {
				return true;
			}
			return true;
		}

	}

	public interface Callback<B> {
		void onComplete(B result, Exception e);
	}

	public interface ProgressCallback {
		void onProgress(Integer p);
	}
}
