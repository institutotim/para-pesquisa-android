Para Pesquisa Android Client
============================

# Requirements to build
* Android Studio Beta 1.5.1
* Android SDK Tools 24.4.1
* Android SDK Platform Tools 23.1
* Android SDK Build-tools 23.0.2
* Android SDK Platform 23
* Android Support Repository 25 (Available via SDK Manager)
* Google Repository 23 (Available via SDK Manager)

# How to import into IDE
After launching Android Studio, go to File -> Open... and search for the folder where the code is.

# How to build outside IDE
Donwload the Android SDK itself, download the required tools and create an environment variable called ANDROID_HOME pointing to it.
After, use the gradle wrapper available in the root folder of project (called **gradlew** or **gradlew.bat**) and run:
```
./gradlew assembleDebug
or
./gradlew assembleRelease
```

Enjoy it!