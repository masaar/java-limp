
# Andriod-limp

This guide details how to integrate Limp SDK into your Andriod app. The Limp Android SDK is compatible with Android 4.0.3 and above.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

You need to add following dependencies into your app before integrating Limp SDK.
For Android Studio 3.3.
```
implementation 'com.squareup.okhttp3:okhttp:3.10.0'
api 'io.jsonwebtoken:jjwt-api:0.10.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.10.5'
runtimeOnly('io.jsonwebtoken:jjwt-orgjson:0.10.5') {
exclude group: 'org.json', module: 'json' 
}
```

## Installing

You can integrate the Limp SDK manually as an SDK.jar.

download and add the limp-android-sdk.jar to the project's class path.

1.First switch your folder structure from Android to Project.
2.Now search for the libs folder inside app - build folder.
3.Once you have pasted the .jar file inside libs folder. Right click on the jar file and at end click on Add as library. This will take care of adding compile files('libs/limp-android-sdk.jar') in build.gradle [You don't have to manually enter this in your build file].

## Add Permissions in AndroidManifest.xml 

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```



## Running the tests


### Initializing 

Set Environment Variables

```
TravelDiv.API().TravelEnvironment.API_URL = “Your Server URL“.
TravelDiv.API().TravelEnvironment.anonToken = “__ANONYMOUS_SECRET_TOKEN_f00000000000000000000012“.
```


You need to call initializer with APIURL , default Token and App Context.

```
Limp.API().init(LimpEnvironment.apiURL, LimpEnvironment.defaultToken, this, new Limp.onResponseListener() {
@Override
public void didDisconnect(Error error) {
// Some error happened.
}

@Override
public void didReceive(Boolean result, SocketResponse response) {
// Server call response
}
});
```

### Authentication

There are three ways to authentication with server. There is enum with name CredentialType.
•    Email
•    Phone 
•    Username
Use password with one of the above to authenticate  with Server.


```
Limp.API().auth(CredentialType.email, "Email", "Password", new Limp.onResponseListener() {
@Override
public void didDisconnect(Error error) {
// Some error happened.

}

@Override
public void didReceive(Boolean result, final SocketResponse response) {
// Server call response
}
});
```

### Reauthentication 

Limp  reauth using  ceched token and  session id.
```
Limp.API().reauth(new Limp.onResponseListener() {
@Override
public void didDisconnect(Error error) {
// Some error happened.
}

@Override
public void didReceive(Boolean result, final SocketResponse response) {
// Server call response
}
});
```

### CheckAuth

To check whether user is already logged in  if not Reauth using current session id .

```
Limp.API().checkauth(new Limp.onResponseListener() {
@Override
public void didDisconnect(Error error) {
// Some error happened.
}
@Override
public void didReceive(Boolean result, final SocketResponse response) {
// Server call response
}
});
```
### Signout

Signout expires your ceched token and session id.

```
Limp.API().logout(new Limp.onResponseListener() {
@Override
public void didDisconnect(Error error) {
// Some error happened.
}

@Override
public void didReceive(Boolean result, final SocketResponse response) {
// Server call response
}
});

```
### Call 

Fetch data from server using call method that takes CallArguments. like Endpoints and query to identify server request, Which type of data you want to get.

```
Map<String, Object> query = new HashMap<>();
Map<String, Object> idValue = new HashMap<>();
idValue.put("val", "Sid");
query.put(":_id", idValue);
CallArguments json = new CallArguments(Endpoints, false, query, doc, context);

Limp.API().call(json, new Limp.onResponseListener() {
@Override
public void didDisconnect(Error error) {
// Some error happened.
}
@Override
public void didReceive(Boolean result, final SocketResponse response) {
// Server call response
}
});

```

## Using Listeners

Limp  also provides interface methods to track when connections Lost and when user received response from server . 

### onResponseListener offers two  protocols to conform :

1. didDisconnect 
Get called When user disconnect from Server.
2. didReceive
Received response from server for every call according to passed parameters in functions mentioned above. 


```
Limp.onResponseListener  listener = new Limp.onResponseListener() {
@Override
public void didDisconnect(Error error) {
// Some error happened.
}

@Override
public void didReceive(Boolean result, SocketResponse response) {
// Server call response
}
};
```
