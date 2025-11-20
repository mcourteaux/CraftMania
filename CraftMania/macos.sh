#!/bin/sh

CP=lib/lwjgl-2.9.3/jar/lwjgl.jar:lib/lwjgl-2.9.3/jar/lwjgl_util.jar:lib/slick-util.jar:src

# Compile
javac -classpath $CP src/org/craftmania/**/*.java

# Run
# (Note the library path pointing to the Linux native folder)
java -classpath $CP -Djava.library.path=./lib/lwjgl-2.9.3/native/macosx/ org.craftmania.CraftMania
