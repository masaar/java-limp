package com.masaar.limp_rxjava;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;


public class ResponseArguments {

    public String callId;
    public ArrayList docs;
    public Integer count;
    public Integer total;
    public Object groups;
    public String code;
    public String _id;
    public Map<String,Object> r_Session;


    public String watch;
    public Map<String,Object> user;
    public String host_add;
    public String user_agent;
    public String timestamp;
    public String expiry;
    public String token;

    public ResponseArguments(Map<String, Object> r_Session) {
        this.r_Session = r_Session;
    }

    public ResponseArguments (String callId, ArrayList docs, Integer count, Integer total, Object groups, String code, String _id, Map<String, Object> r_Session, String watch, Map<String, Object> user, String host_add, String user_agent, String timestamp, String expiry, String token) {
        this.callId = callId;
        this.docs = docs;
        this.count = count;
        this.total = total;
        this.groups = groups;
        this.code = code;
        this._id = _id;
        this.r_Session = r_Session;
        this.watch = watch;
        this.user = user;
        this.host_add = host_add;
        this.user_agent = user_agent;
        this.timestamp = timestamp;
        this.expiry = expiry;
        this.token = token;
    }

    public ResponseArguments(JSONObject object) {
        if (object != null) {
            try {
                Map<String,Object> args = Utilities.toMap((JSONObject) object.get("args"));
                this.code = (String) args.get("code");

                this.callId = (String) args.get("call_id");
                this.watch = (String) args.get("watch");
                this.docs = (ArrayList) args.get("docs");
                this.count = (Integer) args.get("count");
                this.total = (Integer) args.get("total");
                this.groups = args.get("groups");

                this.r_Session = (Map<String, Object>) args.get("session");

                if (r_Session != null) {
                    this._id = (String) r_Session.get("_id");
                    this.timestamp = (String) r_Session.get("timestamp");
                    this.token = (String) r_Session.get("token");
                    this.host_add = (String) r_Session.get("host_add");
                    this.expiry = (String) r_Session.get("expiry");
                    this.user_agent = (String) r_Session.get("user_agent");
                    this.user = (Map<String, Object>) r_Session.get("user");
                }

            } catch (JSONException e) {
//                e.printStackTrace();
                Log.d("ResponseArguments:",e.toString());
            }
        }
    }
}

