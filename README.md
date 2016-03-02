Robopong
========

Like RoboCode, but with Pong instead of tanks.

Requires Java 8 and JavaFX.

## Introduction

### Screenshots!

### Goal

### API

## Ways to play

### Local
Compile and run the LANServer module on the server
Compile and distribute the jar file from Client to everyone who wants to play.

### Web
Compile and run the WebServer module on the server
Compile and distribute the jar file from Client to everyone who wants to play. The players then uploads their source file to the web server. There is no example HTML form for this yet.

## Compiling
These instructions where written using Gradle 2.2.1.

### Compiling the client
This requires JavaFX, and thus Oracle JDK 8

From the git root
```
$ cd Client
$ gradle jar
$ java -jar build/libs/Client-1.0.jar
```
### Compiling the LAN server
This requires JavaFX, and thus Oracle JDK 8

From the git root
```
$ cd LANServer
$ gradle jar
$ java -jar build/libs/LANServer-1.0.jar
```

### Compiling the web server

## Running