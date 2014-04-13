COMM package:
	cp lib/libLinuxSerialParallel.so /usr/lib
	cp jar/comm.jar $JAVAHOME/jre/lib/ext
	cp docs/javax.comm.properties $JAVAHOME/jre/lib
		#LINUX: EDIT the javax.comm.properties file	
		#LINUX: so the serpath0=/dev/ttyUSB0 
