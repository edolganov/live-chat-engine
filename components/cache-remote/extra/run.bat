::-Xms128m -Xmx512m
start "chat-central-server" java -Dhttp.agent=Sync -DuseAsyncLog=true -Dfile.encoding=UTF-8 -classpath "lib\*;" och.comp.cache.server.CacheSever
exit