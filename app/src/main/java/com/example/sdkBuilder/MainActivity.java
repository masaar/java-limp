package com.example.sdkBuilder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.masaar.limp.CallArguments;
import com.masaar.limp.CredentialType;
import com.masaar.limp.Limp;
import com.masaar.limp.LimpEnvironment;
import com.masaar.limp.SocketResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private Button auth , reauth , logout , checkauth , call;
    private TextView output;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = (Button) findViewById(R.id.auth);
        reauth = (Button) findViewById(R.id.reauth);
        logout = (Button) findViewById(R.id.logout);
        call = (Button) findViewById(R.id.call);
        checkauth = (Button) findViewById(R.id.checkAuth);
        output = (TextView) findViewById(R.id.output);
        client = new OkHttpClient();

        LimpEnvironment.apiURL = "ws://api-points.masaar.com/ws";
        LimpEnvironment.defaultToken = "__ANONYMOUS_SECRET_TOKEN_f00000000000000000000012";

        Limp.API().init(LimpEnvironment.apiURL, LimpEnvironment.defaultToken, this, new Limp.onResponseListener() {
            @Override
            public void didDisconnect(Error error) {

            }

            @Override
            public void didReceive(Boolean result, SocketResponse response) {

            }
        });

        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Limp.API().auth(CredentialType.email, "ADMIN@LIMP.MASAAR.COM", "__ADMIN", new Limp.onResponseListener() {
                    @Override
                    public void didDisconnect(Error error) {
                        System.out.print(error);
                    }

                    @Override
                    public void didReceive(Boolean result, final SocketResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                output.setText(response.msg);
                            }
                        });
                    }
                });

            }
        });

        reauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Limp.API().reauth(new Limp.onResponseListener() {
                    @Override
                    public void didDisconnect(Error error) {

                    }

                    @Override
                    public void didReceive(Boolean result, final SocketResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                output.setText(response.msg);
                            }
                        });
                    }
                });
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> query = new HashMap<>();
                Map<String, Object> idValue = new HashMap<>();
                idValue.put("val", "5bd9cbbcabe51c10b7c8f4cf");
                query.put(":_id", idValue);
                CallArguments json = new CallArguments("map/read",true, query,null,getApplicationContext());
                Limp.API().call(json, new Limp.onResponseListener() {
                    @Override
                    public void didDisconnect(Error error) {
                    }
                    @Override
                    public void didReceive(Boolean result, final SocketResponse response) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                output.setText(response.msg);
                            }
                        });
                    }
                });
            }
        });

        checkauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Limp.API().checkauth(new Limp.onResponseListener() {
                    @Override
                    public void didDisconnect(Error error) {

                    }
                    @Override
                    public void didReceive(Boolean result, final SocketResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                output.setText(response.msg);
                            }
                        });
                    }
                });
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Limp.API().logout(new Limp.onResponseListener() {
                    @Override
                    public void didDisconnect(Error error) {

                    }

                    @Override
                    public void didReceive(Boolean result, final SocketResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                output.setText(response.msg);
                            }
                        });
                    }
                });
            }
        });

    }
    Limp.onResponseListener  listener = new Limp.onResponseListener() {
        @Override
        public void didDisconnect(Error error) {

        }

        @Override
        public void didReceive(Boolean result, SocketResponse response) {
            System.out.print(result);
        }
    };
}
