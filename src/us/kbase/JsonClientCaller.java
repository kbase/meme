package us.kbase;

import us.kbase.auth.AuthService;
import us.kbase.auth.AuthToken;

import java.net.*;
import java.io.*;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;

import org.codehaus.jackson.*;
import org.codehaus.jackson.type.*;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonClientCaller {

	private URL urlobj;
	private ObjectMapper mapper;
	private String user = null;
	private char[] password = null;
	private AuthToken accessToken = null;
	private boolean isAuthAllowedForHttp = false;
	private static final String APP_JSON = "application/json";
	
	private static Map<String, AuthToken> user2token = Collections.synchronizedMap(new HashMap<String, AuthToken>());

	public JsonClientCaller(String url) throws MalformedURLException {
		this.urlobj = new URL(url);
		mapper = new ObjectMapper().withModule(new JacksonTupleModule());
	}

	public JsonClientCaller(String url, String accessToken) throws MalformedURLException, IOException {
		this(url);
		this.accessToken = new AuthToken(accessToken);
	}

	public JsonClientCaller(String url, String user, String password) throws MalformedURLException {
		this(url);
		this.user = user;
		this.password = password.toCharArray();
	}

	public boolean isAuthAllowedForHttp() {
		return isAuthAllowedForHttp;
	}
	
	public void setAuthAllowedForHttp(boolean isAuthAllowedForHttp) {
		this.isAuthAllowedForHttp = isAuthAllowedForHttp;
	}
	
	private HttpURLConnection setupCall(boolean authRequired) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) urlobj.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		if (authRequired || user != null || accessToken != null) {
			if (!(conn instanceof HttpsURLConnection || isAuthAllowedForHttp)) {
				throw new IllegalStateException("RPC method required authentication shouldn't be called through unsecured http, " +
						"use https instead or call setAuthAllowedForHttp(true) for your client");
			}
			if (accessToken == null) {
				if (user == null) {
					if (authRequired)
						throw new IllegalStateException("RPC method requires authentication but neither user nor token was set");
				} else {
					accessToken = user2token.get(user);
					if (accessToken != null && !checkTokenExpirationDate(accessToken)) {
						user2token.remove(user);
						accessToken = null;
					}
					if (accessToken == null) {
						try {
							accessToken = requestTokenFromKBase(user, password);
						} catch (Exception ex) {
							try {
								accessToken = requestTokenFromGlobus(user, password);
							} catch (Exception e2) {
								if (authRequired)
									throw e2;
							}
						}
						if (accessToken != null)
							user2token.put(user, accessToken);
					}
				}
			}
			if (accessToken != null)
				conn.setRequestProperty("Authorization", accessToken.toString());
		}
		return conn;
	}

	private static boolean checkTokenExpirationDate(AuthToken token) {
		long expiry = token.getExpiry() * 1000;
		return expiry > System.currentTimeMillis();
	}
	
	public static AuthToken requestTokenFromKBase(String user, char[] password) throws Exception {
		return AuthService.login(user, new String(password)).getToken();
	}
	
	private static AuthToken requestTokenFromGlobus(String user, char[] password) throws Exception,
			MalformedURLException, ProtocolException, JsonParseException,
			JsonProcessingException {
		String authUrl = "https://nexus.api.globusonline.org/goauth/token?grant_type=client_credentials&client_id=rsutormin";
		HttpURLConnection authConn = (HttpURLConnection)new URL(authUrl).openConnection();
		String credential = DatatypeConverter.printBase64Binary((user + ":" + new String(password)).getBytes());
		authConn.setRequestMethod("POST");
		authConn.setRequestProperty("Content-Type", APP_JSON);
		authConn.setRequestProperty  ("Authorization", "Basic " + credential);
		authConn.setDoOutput(true);
		checkReturnCode(authConn);
		InputStream is = authConn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		StringBuilder response = new StringBuilder(); 
		while(true) {
			String line = rd.readLine();
			if (line == null)
				break;
			response.append(line);
			response.append('\n');
		}
		rd.close();
		ObjectMapper mapper = new ObjectMapper();
		JsonParser parser = mapper.getJsonFactory().createJsonParser(new ByteArrayInputStream(response.toString().getBytes()));
		LinkedHashMap<String, Object> respMap = parser.readValueAs(new TypeReference<LinkedHashMap<String, Object>>() {});
		return new AuthToken((String)respMap.get("access_token"));
	}

	private static void checkReturnCode(HttpURLConnection conn) throws Exception {
		int responseCode = conn.getResponseCode();
		if (responseCode >= 300) {
			StringBuilder sb = new StringBuilder();
			InputStream is = conn.getErrorStream();
			if (is == null)
				is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (sb.length() > 0)
					sb.append("\n");
				sb.append(line);
			}
			br.close();
			throw new IllegalStateException("Wrong server response: code=" + responseCode + ", error_message=" + sb);
		}
	}
	
	public <ARG, RET> RET jsonrpcCall(String method, ARG arg, TypeReference<RET> cls, boolean ret, boolean authRequired) throws Exception {
		HttpURLConnection conn = setupCall(authRequired);
		OutputStream os = conn.getOutputStream();
		JsonGenerator g = mapper.getJsonFactory().createJsonGenerator(os, JsonEncoding.UTF8);

		g.writeStartObject();
		g.writeObjectField("params", arg);
		g.writeStringField("method", method);
		g.writeStringField("version", "1.1");
		String id = ("" + Math.random()).replace(".", "");
		g.writeStringField("id", id);
		g.writeEndObject();
		g.close();

		int code = conn.getResponseCode();
		conn.getResponseMessage();

		InputStream istream;
		if (code == 500) {
			istream = conn.getErrorStream();
		} else {
			istream = conn.getInputStream();
		}

		JsonNode node = mapper.readTree(new UnclosableInputStream(istream));
		if (node.has("error")) {
			Map<String, String> ret_error = mapper.readValue(node.get("error"), new TypeReference<Map<String, String>>(){});
			throw new Exception("JSONRPC error received: " + ret_error);
		}
		RET res = null;
		if (node.has("result"))
			res = mapper.readValue(node.get("result"), cls);
		if (res == null && ret)
			throw new Exception("No return found");
		return res;
	}
	
	private static class UnclosableInputStream extends InputStream {
		private InputStream inner;
		private boolean isClosed = false;
		
		public UnclosableInputStream(InputStream inner) {
			this.inner = inner;
		}
		
		@Override
		public int read() throws IOException {
			if (isClosed)
				return -1;
			return inner.read();
		}
		
		@Override
		public int available() throws IOException {
			if (isClosed)
				return 0;
			return inner.available();
		}
		
		@Override
		public void close() throws IOException {
			isClosed = true;
		}
		
		@Override
		public synchronized void mark(int readlimit) {
			inner.mark(readlimit);
		}
		
		@Override
		public boolean markSupported() {
			return inner.markSupported();
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			if (isClosed)
				return 0;
			return inner.read(b);
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (isClosed)
				return 0;
			return inner.read(b, off, len);
		}
		
		@Override
		public synchronized void reset() throws IOException {
			if (isClosed)
				return;
			inner.reset();
		}
		
		@Override
		public long skip(long n) throws IOException {
			if (isClosed)
				return 0;
			return inner.skip(n);
		}
	}
}
