package com.masaar.limp_rxjava;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
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
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import java.security.Key;
import java.util.concurrent.TimeUnit;
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
    private ResponseArguments session;
    private String nonToken;
    private Header header = Jwts.header();
    private onResponseListener responseListener;

    private Observable<Long> heartbeat = Observable.interval(30, TimeUnit.SECONDS);
    private Boolean appActive = true;

    private Boolean inited = false;
    private BehaviorSubject<Boolean> inited$ = BehaviorSubject.create();

    public Boolean authed = false;
    public BehaviorSubject<ResponseArguments> authed$ = BehaviorSubject.create();

//    private String name;
//    private Long size;
//    private String type;
//    private String lastModified;
//    private int[] content;
//
//    private InputStream inputStream;
//    private OutputStream outputStream;
//    private ObjectOutputStream objectOutputStream;

    private LimpFile limpFile;
    private File UPLOAD_FILE_PATH;


    public interface onResponseListener {
          void  didDisconnect(Observable<Error> error);
          void didReceive(Observable<Boolean> result,Observable<SocketResponse> response);
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
                Log.d("JSONException" , e.toString());
            }

            responseListener.didReceive(Observable.just(true), Observable.just(socketResponse));

            CallArguments arguments = new CallArguments();
            if ((socketResponse.args != null) && socketResponse.args.callId == arguments.call_id) {
                Log.d("call_id", "message received from observer on call_id:");
                if (socketResponse.status != null){
                if (socketResponse.status == 200) {

                }}
                if ((socketResponse.args.watch) != null) {
                    Log.d("call_id", "completing the observer with call_id:");
                } else {
                    Log.d("call_id", "Detected watch with call_id: ");
                }
            }


            String code = socketResponse.args.code;
            if (code != null) {
                if (code.equals("CORE_CONN_READY")) {
                    reset(false);
                    nonToken = SDKConfig.nonToken;
                    CallArguments callArguments = new CallArguments("conn/verify", authed, null, null, context);
                    call(callArguments, responseListener);

                } else if (code.equals("CORE_CONN_OK")) {
                    inited = true;
                    inited$.onNext(true);
                    checkHeartbeat();
                } else if (code.equals("CORE_CONN_CLOSED")) {
                    reset(false);
                } else if (socketResponse.args.r_Session != null) {
                    Log.d("socketResponse", "Response has session obj");
                    String sid = (String) socketResponse.args.r_Session.get("_id");
                    if (sid == "f00000000000000000000012") {
                        if (authed) {
                            authed = false;
                            session.r_Session.clear();
                            authed$.onNext(new ResponseArguments(session.r_Session = null));
                            SharedPreferences.Editor editor = sharedPreferencess.edit();
                            editor.clear();
                            Log.d("Session", "Session is null");
                        }
                    } else {
                        String id = (String) socketResponse.args.r_Session.get("_id");
                        String token = (String) socketResponse.args.r_Session.get("token");
                        SharedPreferences.Editor editor = sharedPreferencess.edit();
                        editor.putString("token", token);
                        editor.putString("sid", id);
                        editor.apply();
                        authed = true;
                        session.r_Session = socketResponse.args.r_Session;
                        authed$.onNext(new ResponseArguments(session.r_Session));
                        Log.d("Session", "Session updated");
                    }
                }
            }

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
            responseListener.didDisconnect(Observable.just(error));
        }

    }


    public  Limp() {
        header.put("alg","HS256");
        header.put("typ","JWT");
    }

    public void checkHeartbeat(){
        this.inited$.asObservable().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean initt) {
                if (initt){
                    heartbeat.asObservable().subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            CallArguments arguments = new CallArguments("heart/beat", authed, null, null ,context);
                            Limp.this.call(arguments, new onResponseListener() {
                                @Override
                                public void didDisconnect(Observable<Error> error) {
                                    Log.d("Observable<Error>","Error:"+ error);
                                }

                                @Override
                                public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
                                    Log.d("SocketResponse","heart beat complete..");
                                }
                            });
                        }
                    });

                }

            }
        });


        ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(myProcess);
        Boolean isInBackground = myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

        if (!isInBackground){
            appActive = true;
        }else {
            appActive = false;
            heartbeat.asObservable().subscribe().isUnsubscribed();
        }

    }

    public void init(SDKConfig config ,Context context , onResponseListener response) {

        if (SDKConfig.authAttrs != null){
            Log.d("SDKConfig","SDK Auth not set");
        }
            Log.d("SDKConfig","Resetting SDK before init.");

        reset(false);

        this.responseListener = response;
        this.context = context;
        sharedPreferencess =  context.getSharedPreferences("cachedValue", Context.MODE_PRIVATE);
        Request request = new Request.Builder().url(config.apiURL).build();
        OkHttpClient client = new OkHttpClient();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        socket = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();


    }

    public void close (final onResponseListener responseListener){

        CallArguments arguments = new CallArguments("conn/close", this.authed, null, null ,context);
        call(arguments, new onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
                responseListener.didDisconnect(error);
            }

            @Override
            public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
                responseListener.didReceive(result,response);
            }
        });


    }

    public void reset(Boolean forceInited){
        authed = false;

        if (session != null && session.r_Session != null){
            session.r_Session.clear();
            authed = false;
            authed$.onNext(new ResponseArguments(session.r_Session = null));
        }

        if (forceInited || inited){

            inited = false;
            inited$.onNext(false);
        }
    }

    public Object isAuthed() {
        return session;
    }

    public void deleteWatch(String watch ,final onResponseListener responseListener){
        SocketResponse socketResponse = new SocketResponse();
        watch = socketResponse.args.watch;
        if (watch == null){
            watch = "__all";
        }
        Map<String, Object> query = new HashMap<>();
        ArrayList<Map<String, Object>> queryList = new ArrayList<>();
        query.put("watch",watch);
        queryList.add(query);
        CallArguments json = new CallArguments("watch/delete",this.authed, queryList,null,context);
        call(json, new onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
                responseListener.didDisconnect(error);
            }

            @Override
            public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
                responseListener.didReceive(result,response);
            }
        });
    }

    private String generateAuthHash(CredentialType authVar, String authVal , String password) {

        ArrayList<String> hashArray = new ArrayList<>();
        hashArray.add(authVar.name());
        hashArray.add(authVal);
        hashArray.add(password);
        if (SDKConfig.authHashLevel == 5.6) {
            hashArray.add(SDKConfig.nonToken);
        }

        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String hash =  Jwts.builder().setHeader((Map<String, Object>) header).claim("hash", hashArray).signWith(key).compact();
        return  hash.split(Pattern.quote("."))[1];
    }

    public void auth(CredentialType authVar, String authVal , String password, final onResponseListener responseListner){

        if (SDKConfig.authAttrs != null) {
            if (!SDKConfig.authAttrs.toString().contains(authVar.name())) {
                Log.d("SDKConfig.authAttrs", "unkown authVar");
            }
        }

        String hash = generateAuthHash(authVar, authVal, password);
        Map<String, Object> doc = new HashMap<>();
        doc.put(authVar.name(), authVal);
        doc.put("hash", hash);

        this.authed = false ;
        SharedPreferences.Editor editor = sharedPreferencess.edit();
        editor.clear();
        this.session = null;

        CallArguments arguments = new CallArguments("session/auth", this.authed, null, doc ,context);
        call(arguments, new onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
                responseListner.didDisconnect(error);
            }

            @Override
            public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
                response.asObservable().subscribe(new Action1<SocketResponse>() {
                    @Override
                    public void call(SocketResponse response) {
                        authed =  true;
                        String token = (String) response.args.r_Session.get("token");
                        String sId = (String) response.args.r_Session.get("_id");
                        SharedPreferences.Editor editor = sharedPreferencess.edit();
                        editor.putString("token", token);
                        editor.putString("sid", sId);
                        editor.apply();
                        responseListner.didReceive(Observable.just(true),Observable.just(response));
                    }
                });

            }
        });
    }


    public void reauth(final onResponseListener responseListener){
        String cachedToken = sharedPreferencess.getString("token","__ANON_TOKEN_f00000000000000000000012");
        String cachedSid =   sharedPreferencess.getString("sid","f00000000000000000000012");
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
        ArrayList<Map<String, Object>> queryList = new ArrayList<>();
        query.put("_id", cachedSid);
        query.put("hash", smallHash);
        queryList.add(query);
        CallArguments json = new CallArguments("session/reauth",this.authed, queryList,null,context);
        call(json, new onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
                responseListener.didDisconnect(error);
            }

            @Override
            public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
                responseListener.didReceive(result,response);
            }
        });
    }

    public void logout(final onResponseListener responseListener){
        String cachedSid = sharedPreferencess.getString("sid","f00000000000000000000012");
        Map<String, Object> query = new HashMap<>();
        ArrayList<Map<String, Object>> queryList = new ArrayList<>();
        query.put("_id",cachedSid);
        queryList.add(query);
        CallArguments json = new CallArguments("session/signout",this.authed, queryList,null,context);
        call(json, new onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
                responseListener.didDisconnect(error);
            }

            @Override
            public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
                responseListener.didReceive(result,response);
            }
        });
    }


    public void checkauth(final onResponseListener responseListener){
        String cachedToken = sharedPreferencess.getString("token","__ANON_TOKEN_f00000000000000000000012");
        String cachedSid = sharedPreferencess.getString("sid","f00000000000000000000012");
        if (cachedToken == null || cachedSid == null) {
            responseListener.didReceive(Observable.just(false),Observable.just(new SocketResponse(null,"No credetial cached.",null)));
        }else {
            this.reauth(new onResponseListener() {
                @Override
                public void didDisconnect(Observable<Error> error) {
                    responseListener.didDisconnect(error);
                }

                @Override
                public void didReceive(final Observable<Boolean> result, final Observable<SocketResponse> response) {

                    response.subscribe(new Action1<SocketResponse>() {
                        @Override
                        public void call(SocketResponse socketResponse) {
                            if (socketResponse.status == 500 || socketResponse.status == 403){
                                responseListener.didReceive(Observable.just(false),Observable.just(new SocketResponse(403,"Wrong credential cached.",null)));
                            }else {
                                authed = true;
                                session.r_Session = socketResponse.args.r_Session;
                                responseListener.didReceive(result,response);
                            }
                        }
                    });

                }
            });
        }
        this.responseListener = responseListener;
    }



    public void call(CallArguments callArguments, final onResponseListener responseListner){
        String token = callArguments.token;
        byte[] bytes = new byte[0];
        try {
            bytes = token.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SecretKeySpec keystuff = new SecretKeySpec(bytes, 0, bytes.length, "HmacSHA256");
        Map<String, Object> claims = callArguments.getClaims();
        String hash =  Jwts.builder().setHeader((Map<String, Object>) header).setClaims(claims).signWith(SignatureAlgorithm.HS256,keystuff).compact();
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


    public void createFile(int resourceId , String name , String jobtitle , String bio , final onResponseListener responseListner){

        TypedValue value = new TypedValue();
        context.getResources().getValue(resourceId, value, true);
        String resourceName = value.string.toString();
        int lastIndexOf = resourceName.lastIndexOf("/");
        if (lastIndexOf == -1) {
            resourceName = "";
        }
        String finalName = resourceName.substring(lastIndexOf);
        UPLOAD_FILE_PATH = new File(context.getFilesDir() + File.separator + finalName);
        limpFile = new LimpFile(UPLOAD_FILE_PATH.getAbsolutePath());

        Map<String, Object> doc = new HashMap<>();

        Map<String, Object> docName = new HashMap<>();
        docName.put("ar_AE",name);
        Map<String, Object> docJobtitle = new HashMap<>();
        docJobtitle.put("ar_AE",jobtitle);
        Map<String, Object> docBio = new HashMap<>();
        docBio.put("ar_AE",bio);

        doc.put("photo", limpFile.getDocObject());
        doc.put("name", docName);
        doc.put("jobtitle",docJobtitle);
        doc.put("bio",docBio);

        CallArguments arguments = new CallArguments("staff/create", this.authed, null, doc ,context);
        call(arguments, new onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
                responseListener.didDisconnect(error);
            }

            @Override
            public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
                responseListener.didReceive(result,response);
            }
        });



        this.responseListener = responseListner;
    }

}