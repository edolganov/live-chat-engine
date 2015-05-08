::-Xms128m -Xmx512m
start "chat-central-server" java -Dhttp.agent=Sync -DuseAsyncLog=true -Dfile.encoding=UTF-8 -agentlib:jdwp=transport=dt_socket,address=12170,server=y,suspend=n -classpath "lib\*;" och.comp.cache.server.CacheSever
exit