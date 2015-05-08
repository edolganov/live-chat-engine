#! /bin/bash

#if [ ! -e "log.txt" ] ; then
# touch "log.txt"
# chmod 664 log.txt
#fi

#JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
#PATH=$JAVA_HOME/bin:$PATH

#-Xms128m -Xmx512m
nohup java -Xms64m -Xmx512m -Dlog4j.configuration=file:log4j.properties -DuseAsyncLog=true -Dhttp.agent=Sync -Dfile.encoding=UTF-8 -classpath "lib/*" och.comp.cache.server.CacheSever > /dev/null 2>&1 &