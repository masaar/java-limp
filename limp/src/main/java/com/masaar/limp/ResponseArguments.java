package com.masaar.limp;

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

    public ResponseArguments(String callId, ArrayList docs, Integer count, Integer total, Object groups, String code) {
        this.callId = callId;
        this.count = count;
        this.total = total;
        this.code = code;
        this.docs = docs;
        this.groups = groups;
    }

    public ResponseArguments(JSONObject object) {
        if (object != null) {
            try {
                Map<String,Object> args = Utilities.toMap((JSONObject) object.get("args"));
                this.callId = (String) args.get("call_id");
                this.docs = (ArrayList) args.get("docs");
                this.count = (Integer) args.get("count");
                this.total = (Integer) args.get("total");
                this.groups = args.get("groups");
                this.code = (String) args.get("postal_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

