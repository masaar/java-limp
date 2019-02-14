package com.masaar.limp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Header;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.regex.Pattern;

import javax.crypto.spec.SecretKeySpec;



public class Limp {

    // Shared instance
    private static final Limp SINGLE_INSTANCE = new Limp();
    public static Limp API() {
        return SINGLE_INSTANCE;
    }

    // Private variables
    private Context context;
    private SharedPreferences sharedPreferencess;
    private WebSocket socket;
    private Object session = null;
    private String defaultToken;
    private Header header = Jwts.header();
    private onResponseListener responseListener;

    public Boolean authed = false;

    public interface onResponseListener {
        void  didDisconnect(Error error);
        void  didReceive(Boolean result, SocketResponse response);
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            SocketResponse socketResponse = null;
            try {
                JSONObject json= (JSONObject) new JSONTokener(text).nextValue();
                Integer status = (Integer) json.get("status");
                String msg = (String) json.get("msg");
                socketResponse = new SocketResponse(status, msg);
                ResponseArguments responseArguments = new ResponseArguments(json);
                socketResponse.setArgs(responseArguments);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            responseListener.didReceive(true, socketResponse);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            // TODO: handle bytes response
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Error error = new Error("Connection Error");
            responseListener.didDisconnect(error);
        }

    }

    public  Limp() {
        header.put("alg","HS256");
        header.put("typ","JWT");
    }

    public void init(String apiURL, String defaultToken, Context context, onResponseListener response) {
        //ws://api-points.masaar.com/ws
        this.defaultToken = defaultToken;
        this.responseListener = response;
        this.context = context;
        sharedPreferencess =  context.getSharedPreferences("cachedValue", Context.MODE_PRIVATE);
        Request request = new Request.Builder().url(apiURL).build();
        OkHttpClient client = new OkHttpClient();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        socket = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }

    private String generateAuthHash(CredentialType authVar, String authVal , String password) {
        String[] hashArray = {authVar.name(), authVal, password};
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String hash =  Jwts.builder().setHeader((Map<String, Object>) header).claim("hash", hashArray).signWith(key).compact();
        return  hash.split(Pattern.quote("."))[1];
    }

    public void auth(CredentialType authVar, String authVal , String password, final onResponseListener responseListner){

        String hash = generateAuthHash(authVar, authVal, password);
        Map<String, Object> doc = new HashMap<>();
        doc.put(authVar.name(), authVal);
        doc.put("hash", hash);

        this.authed = false ;
        this.session = null;

        SharedPreferences.Editor editor = sharedPreferencess.edit();
        editor.clear();
        CallArguments arguments = new CallArguments("session/auth", this.authed, null, doc ,context);
        call(arguments, new onResponseListener() {

            @Override
            public void didDisconnect(Error error) {
                responseListner.didDisconnect(error);
            }

            @Override
            public void didReceive(Boolean result, SocketResponse response) {
                authed =  true ;
                session = response.args.docs.get(0);
                ArrayList docs = response.args.docs;
                Map<String,Object> dic = (Map<String, Object>) docs.get(0);
                String token  = (String) dic.get("token");
                String sId   = (String) dic.get("_id");
                SharedPreferences.Editor editor = sharedPreferencess.edit();
                editor.putString("token",token);
                editor.putString("sid",sId);
                editor.apply();
                responseListner.didReceive(true,response);
            }
        });

    }

    public void reauth(final onResponseListener responseListener){
        String cachedToken = sharedPreferencess.getString("token","__ANONYMOUS_SECRET_TOKEN_f00000000000000000000012");
        String cachedSid =   sharedPreferencess.getString("sid","f00000000000000000000012");
        //Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        byte[] bytes = new byte[0];
        try {
            bytes = cachedToken.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SecretKeySpec keystuff = new SecretKeySpec(bytes, 0, bytes.length, "HmacSHA256");
        String hash =  Jwts.builder().setHeader((Map<String, Object>) header).claim("token",cachedToken).signWith(SignatureAlgorithm.HS256,keystuff).compact();
        String smallHash  = hash.split(Pattern.quote("."))[1];
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> idValue = new HashMap<>();
        Map<String, Object> hashValue = new HashMap<>();
        idValue.put("val", cachedSid);
        hashValue.put("val",smallHash);
        query.put("_id", idValue);
        query.put("hash", hashValue);
        CallArguments json = new CallArguments("session/reauth",this.authed, query,null,context);
        call(json, responseListener);
    }

    public Object isAuthed() {
        return session;
    }

    public void checkauth(final onResponseListener responseListener){
        String cachedToken = sharedPreferencess.getString("token","__ANONYMOUS_SECRET_TOKEN_f00000000000000000000012");
        String cachedSid = sharedPreferencess.getString("sid","f00000000000000000000012");
        if (cachedToken == null || cachedSid == null) {
            responseListener.didReceive(false, new SocketResponse(null,"No credetial cached.",null));
        }else {

            this.reauth(new onResponseListener() {
                @Override
                public void didDisconnect(Error error) {
                    responseListener.didDisconnect(error);
                }

                @Override
                public void didReceive(Boolean result, SocketResponse response) {
                    if (response.status == 500 || response.status == 403) {
                        responseListener.didReceive(false,new SocketResponse(403,"Wrong credential cached.",null)); }
                    else {
                        authed = true;
                        session = response.args.docs.get(0);
                        responseListener.didReceive(result,response);
                    }
                }
            });
        }
    }

    public void logout(final onResponseListener responseListener){
        String cachedSid = sharedPreferencess.getString("sid","f00000000000000000000012");
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> idVal = new HashMap<>();
        idVal.put("val",cachedSid);
        query.put("_id",idVal);
        CallArguments json = new CallArguments("session/signout",this.authed, query,null,context);
        call(json, responseListener);
    }

    public void call(CallArguments allArguments, final onResponseListener responseListner){
        String token = allArguments.token;
        byte[] bytes = new byte[0];
        try {
            bytes = token.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SecretKeySpec keystuff = new SecretKeySpec(bytes, 0, bytes.length, "HmacSHA256");
        String hash =  Jwts.builder().setHeader((Map<String, Object>) header).setClaims(allArguments.getClaims()).signWith(SignatureAlgorithm.HS256,keystuff).compact();
        String finalString =  "{'token':"+hash+"}";
        JSONObject json= null;
        try {
            json = (JSONObject) new JSONTokener(finalString).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.send(String.valueOf(json));
        this.responseListener = responseListner;
    }
}