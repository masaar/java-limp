package com.masaar.limp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CallArguments extends Application {

    public String call_id;
    public String endpoint;
    public String sid;
    public String token;

    Map<String, Object> query;
    Map<String, Object> doc;

    public SharedPreferences sharedPreferencess;


    public CallArguments(String endpoint, Boolean authed, Map<String,Object> query, Map<String,Object> doc ,Context context) {
        Random random = new Random();
        final int min = 0;
        final int max = 36;
        String randomStr = ""+random.nextInt((max - min) + 1);
        this.call_id = randomStr;

        sharedPreferencess =  context.getSharedPreferences("cachedValue", Context.MODE_PRIVATE);
        String cachedToken = sharedPreferencess.getString("token","__ANONYMOUS_SECRET_TOKEN_f00000000000000000000012");
        String cachedSid =   sharedPreferencess.getString("sid","f00000000000000000000012");
        this.endpoint = endpoint;
        if (authed == true) {
        this.sid = cachedSid;
        this.token = cachedToken;}else {
            this.sid = "f00000000000000000000012";
            this.token = "__ANONYMOUS_SECRET_TOKEN_f00000000000000000000012";
        }
        this.doc = doc;
        this.query = query;
        if (this.query == null) {
            this.query = new HashMap<String, Object>();
        }
    }

    Map<String, Object> getClaims() {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("call_id", call_id);
        claims.put("endpoint", endpoint);
        claims.put("sid", sid);
        claims.put("token", token);
        claims.put("query", query);
        claims.put("doc", doc);

        return claims;
    }
}

