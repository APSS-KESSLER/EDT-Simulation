all: bin/brownshome_apss_UnderlyingModels.dll

bin/brownshome_apss_UnderlyingModels.dll: bin/brownshome_apss_UnderlyingModels.o bin/GeomagnetismLibrary.o
	gcc -shared -o bin/brownshome_apss_UnderlyingModels.dll bin/brownshome_apss_UnderlyingModels.o bin/GeomagnetismLibrary.o

bin/GeomagnetismLibrary.o:
	gcc -std=c11 -c -o bin/GeomagnetismLibrary.o src/c/WMM/GeomagnetismLibrary.c
	
bin/brownshome_apss_UnderlyingModels.o: bin/brownshome_apss_UnderlyingModels.h
	gcc -std=c11 -c -o bin/brownshome_apss_UnderlyingModels.o -I"%JAVA_HOME%/include/win32" -I"src/c/WMM" -I"%JAVA_HOME%/include" src/c/brownshome_apss_UnderlyingModels.c

bin/brownshome_apss_UnderlyingModels.h: bin/brownshome/apss/UnderlyingModels.class
	javah -d bin -classpath bin brownshome.apss.UnderlyingModels

bin/brownshome/apss/UnderlyingModels.class:
	javac -encoding utf-8 src/java/brownshome/apss/UnderlyingModels.java -d bin -sourcepath src/java