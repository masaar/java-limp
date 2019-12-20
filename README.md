
# limp-rxjava

Official ReactiveX Android SDK for LIMP.

## Quick Start

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Install limp-rxjava for ReactiveX

You need to add following dependencies into your app before integrating Limp SDK.
For Android Studio 3.5.2
```
implementation 'com.squareup.okhttp3:okhttp:3.10.0'
implementation 'io.reactivex:rxandroid:1.2.1'
implementation 'io.reactivex:rxjava:1.3.0'
api 'io.jsonwebtoken:jjwt-api:0.10.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.10.5'
runtimeOnly('io.jsonwebtoken:jjwt-orgjson:0.10.5') {
exclude group: 'org.json', module: 'json' 
}
```

## Installing

You can integrate the Limp SDK manually as an SDK.jar.

download and add the limp-rxjava.jar to the project's class path.

1.First switch your folder structure from Android to Project.
2.Now search for the libs folder inside app - build folder.
3.Once you have pasted the .jar file inside libs folder. Right click on the jar file and at end click on Add as library. This will take care of adding compile files('libs/limp-rxjava.jar') in build.gradle [You don't have to manually enter this in your build file].

## Add Permissions in AndroidManifest.xml 

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

## How to Use

### Initializing 

You need to call initializer with SDKConfig object and App Context.

```
Limp.API().init(new SDKConfig(), this, new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
			// some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
			// server call response
}
});
```

### SDK Config

When initialising the SDK, you should pass an object matching the interface SDKConfig, which has the following attributes:

api (Required): The URI of LIMP app you are connecting to.
anonToken (Required): LIMP app ANON_TOKEN.
authAttrs (Required): As of LIMP APIv5.8, LIMP apps don't have strict User module attrs structure. This includes the authentication attrs that are set per app. This attribute represents an String[] referring to the same authentication attrs of the app.
authHashLevel (Optional): Either 5.0 or 5.6. With the change to auth hash generation introduced in APIv5.6 of LIMP, some legacy apps are left without the ability to upgrade to APIv5.6 and beyond due to hashes difference. SDKv5.7 is adding authHashLevel to allow developers to use higher APIs and SDKs with legacy apps. Default 5.6;

## Best Practices 

You can use the SDK 100% per your style of development, however we have some tips:

### Session Reauth

The best practice to handle a reauth scenario is by attempting to checkAuth as soon as the connection with LIMP app is made. This can be made by subscribing to inited$ subject which notifies subscriptions about any changes to SDK initialisation status reflected as inited attribute in the SDK. Which can be done like:

```
Limp.API().inited$.asObservable().subscribe(new Action1<Boolean>() {
@Override
public void call(Boolean init) {
    if (init){
    	// SDK is inited and ready for your calls:
        Limp.API().checkauth(new Limp.onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
            	// some error happened.
            }

            @Override
            public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
            	// server call response
            }
        });
    }
}
});
```

### Auth State Detection

Although, you can detect the user auth state in the subscription of the calls auth, reauth and checkAuth, the best practice is to use the global authed$ state Subject. You can do this by subscripting to authed$ in the same component (usually AppComponent) you are initiating the SDK at. This assures a successful checkAuth as part of the api.init subscription can be handled. The model suggested is:

```
Limp.API().authed$.asObservable().subscribe(new Action1<ResponseArguments>() {
@Override
public void call(ResponseArguments responseArguments) {
        if (responseArguments.r_Session != null){
            Log.d("auth","We are having an `auth` condition with session:"+responseArguments.r_Session);
    }else {
            Log.d("auth", "We just got unauthenticated");
        }
}
});
```

### Reconnecting on Disconnects

Websockets are always-alive connections. A lot can go wrong here resulting in the connection with your LIMP app. To make sure you can always get reconnected recall SDK init method upon SDK becoming not inited:

```
Limp.API().inited$.asObservable().subscribe(new Action1<Boolean>() {
@Override
public void call(Boolean init) {
    if (init){
    	// SDK is inited and ready for your calls:
        Limp.API().checkauth(new Limp.onResponseListener() {
            @Override
            public void didDisconnect(Observable<Error> error) {
            	// some error happened.
            }

            @Override
            public void didReceive(Observable<Boolean]> result, Observable<SocketResponse> response) {
            	// server call response
            }
        });
    }else {
	Limp.API().init(new SDKConfig(), this, new Limp.onResponseListener() {
	@Override
	public void didDisconnect(Observable<Error> error) {

	}

	@Override
	public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {

	}
	});
	}
}
});
```

## API Reference

### session 

A Response Argument object representing the current session. It has value only when the user is authenticated.

### authed 

A boolen storing the current state of user authentication.

### authed$

A BehaviorSubject<ResponseArguments> you can subscribe to handle changes to state of user authentication.

### init()

The base method to initiate a connection with LIMP app. This method returns an Observable for chain subscription if for any reason you need to read every message being received from the API, however subscribing to it is not required. Method definition:

```
Limp.API().init(new SDKConfig(), this, new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
			// some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
			// server call response
}
});
```

### close()

The method to close current connection with LIMP app. This method returns an Observable for chain subscription if for any reason you need to read the resposne of the close call, however subscribing to it is not required. Method definition:

```
Limp.API().close(new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
	// some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
   // server call response
}
});
```

### auth()

The method you can use to authenticate the user. This method returns an Observable for chain subscription if for any reason you need to read the response of the auth call, however subscribing to it is not required. Method definition:

```
Limp.API().auth(CredentialType.email,"ADMIN@LIMP.MASAAR.COM","__ADMIN", new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
            // some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
            // server call response
}
});
```


### reauth()
The method you can use to reauthenticate the user. The method would fail if no sid and token attrs are cached from earlier successful authentication call. This method returns an Observable for chain subscription if for any reason you need the response of the reauth call, however subscribing to it is not required. Method definition:

```
Limp.API().reauth(new Limp.onResponseListener() {
    @Override
    public void didDisconnect(Observable<Error> error) {
        // some error happened.
    }

    @Override
    public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
    	// server call response
    }
});
```

### Signout()

The method you can use to signout the current user. Upon success, this methods removes all the cached attrs of the session. This method returns an Observable for chain subscription if for any reason you need the response of the signout call, however subscribing to it is not required. Method definition:

```
Limp.API().signout(new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
	// some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
   // server call response
}
});
```

### CheckAuth()

The method to check whether there is a cached session and attempt to reauthenticate the user. This method would return an error if no credentials are cached. This method returns an Observable for chain subscription if for any reason you need the response of the checkAuth call, however subscribing to it is not required. Method definition:

```
Limp.API().checkauth(new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
	// some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
   // server call response
}
});
```

### generateAuthHash()

The method to use to generate authentication hashes. This is used internally for the auth() call. However, you also need this to generate the values when creating a user. Method definition:

```generateAuthHash(CredentialType authVar, String authVal , String password){/*...*/}```

### deleteWatch()

The method to delete a watch in progress. You can pass the watch ID you want to delete or ```__all``` to delete all watches. This method returns an Observable for chain subscription if for any reason you need the response of the deleteWatch call, however subscribing to it is not required. Method definition:

```
Limp.API().deleteWatch("__all", new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
    // some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
	 // server call response
}
});
```

### Call 

The most important method in the SDK. This is the method you use to call different endpoints in your LIMP app. Although the callArgs object in the params is having full definition of all call attrs, you still usually only need to pass either query and/or doc in most of the cases. Method definition:

```
CallArguments callArgs = new CallArguments(Endpoints, false, query, doc, context);
Limp.API().call(callArgs, new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
    // some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
	// server call response
}
});

```

## Using Listeners

Limp also provides interface methods to track when connections Lost and when user received response from server . 

### onResponseListener offers two  protocols to conform :

1. didDisconnect 
Get called When user disconnect from Server.
2. didReceive
Received response from server for every call according to passed parameters in functions mentioned above. 

```
Limp.onResponseListener listener = new Limp.onResponseListener() {
@Override
public void didDisconnect(Observable<Error> error) {
	// some error happened.
}

@Override
public void didReceive(Observable<Boolean> result, Observable<SocketResponse> response) {
	// server call response
}
};
```
