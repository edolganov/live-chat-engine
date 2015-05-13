#! /bin/bash

export ANT_HOME=.\thirdparty\ant
export ANT_OPTS=$ANT_OPTS -Dfile.encoding=UTF-8

./thirdparty/ant/bin/ant -f build.xml