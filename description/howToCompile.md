

## Compile with java 8

/usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/javac -cp "lib/*" -d bin src/*/*.java

## Run with whatever java
java -cp "lib/*:bin" logist.LogistPlatform config/centralized.xml centralized-random