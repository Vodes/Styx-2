# Styx 2 Desktop Client

The new native desktop client for yet another mediaserver stack.

## Features & Architecture
- Entire UI written in Kotlin using Compose Multiplatform
	<br>With most of the code located in a [common](https://github.com/Vodes/Styx-Common) and a [common-compose](https://github.com/Vodes/Styx-Common-Compose) library
- Material 3 Design
	<br>Loosely following official guidelines
- No constant connection required
	- All data is synced on startup or with a manual refresh[^1]<br>
	- All images are cached locally[^1]<br>
	- Watch progress and favourites are always local-first and synced to the server when a connection is possible

#### Desktop specific
- Bundled mpv player (on windows) and a somewhat custom config<br>
- Player controlled through json-ipc and reading stdout
	<br>So you can also use your own install with your own config if desired

## Screenshots
<details>
	<summary>Click here</summary>

 #### Search
 ![search](https://i.ibb.co/jLyzCvt/java-MZr-Msm-Msu-Q.webp)
 
 #### Show detail view
 ![detail view](https://i.ibb.co/9ZsfxCV/java-Eo-VTH9-B4-Qg.webp)

 #### Settings
 ![settings](https://i.ibb.co/xLvHqVK/java-NSb-QJld4qg.webp)
</details>

## How do I use this?
<details>
	<summary>Short answer</summary>
	You don't.
</details>

<details>
	<summary>Long answer</summary>
  
There is no public instance for this.<br>
You will have to build every part of the [ecosystem](https://github.com/Vodes?tab=repositories&q=Styx&language=kotlin) yourself and run it on your own server.
</details>


## How to run/build
Building is as simple as running one of these, assuming you have set your buildconfig correctly and whatnot.
```bash
# Windows
./gradlew clean packageReleaseMsi packageReleaseUberJarForCurrentOS

# Linux
./gradlew clean packageReleaseDeb packageReleaseRpm packageReleaseUberJarForCurrentOS
```
To my knowledge, compose-mp does not currently support cross compiling for desktop yet so you are going to have to run this on their respective OS.<br>
You can also do so with github workflows. See the workflows directory in this repo.<br>

Running is just `./gradlew run`

[^1]: ##### I realize this may be infeasible when working with a huge library but this is not a concern for me and I'm building this just for me. With my current library of ~10TB I'm sitting at around 30MB of cached images and ~7MB of other data on the clientside.
