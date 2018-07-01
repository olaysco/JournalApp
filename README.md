# journalApp
Android(JAVA) App that allows authenticated (Email/Password or Google sign in )user to write down their daily entries, data is stored in the cloud(Cloud Firestore) and can be access on other logged in devices with the app.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

What things you need to install the software and how to install them

```
Internet conection to install required dependencies

    - 'com.android.support:appcompat-v7:27.0.2'
    - 'com.android.support.constraint:constraint-layout:1.0.2'
    - 'com.android.support:appcompat-v7:27.0.2'
    - 'com.android.support:cardview-v7:27.0.2'
    - 'com.android.support:recyclerview-v7:27.0.2'
    - 'com.android.support:design:27.0.2'
    - 'com.android.support:support-v4:27.0.2'
    - 'com.google.firebase:firebase-core:16.0.1'
    - 'com.google.firebase:firebase-auth:16.0.2'
    - 'com.google.firebase:firebase-firestore:17.0.2'
    - 'com.firebaseui:firebase-ui-firestore:3.1.0'
    - 'com.google.android.gms:play-services-auth:15.0.1'
    - "android.arch.lifecycle:extensions:1.1.0"
    - "android.arch.lifecycle:compiler:1.1.0"
    
    This is provided you for information purpose, you dont need to include them yourself
```


### Installing

Installation steps are

```
- Clone or download the project to your local directory
- From Android Studio click on import project, and locate the folder the project you cloned or downloaded the projct to
```

```
Go to String resource file and locate **<string name="web_client_id></string>"** input your own google web client id
```


## Running the tests

Run UI test with espresso
Run Lint using the Android Lint Utility
Test Class can be found in the __androidTest/Java__ folder





## Built With

Android Studio 3.0.1

## Contributing

feel free to contribute
 

## Authors

* **Odunsi Olayiwola** - @olaysco



## License

This project is licensed under the General public license - see the [LICENSE.md](LICENSE.md) file for details


