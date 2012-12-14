#!/bin/bash
. /etc/profile

min_heap_size="64m"
max_heap_size="128m"

SCRIPT="$0"
while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  # Drop everything prior to ->
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

targetdir=`dirname "$SCRIPT"`
targetdir=`cd $targetdir; pwd`
serverdir=${targetdir%/*}
serverjar=$serverdir/server.jar
cd $serverdir


start()
{
  nohup java -Xms$min_heap_size -Xmx$max_heap_size -XX:PermSize=128m -Xloggc:gc.log -XX:+PrintGCTimeStamps -XX:-PrintGCDetails -Dfile.encoding=UTF-8 -jar $serverjar  > server.log  &
  echo $! > service_framework.pid
}
stop()
{
  kill  `cat service_framework.pid`
}

case $1 in
"restart")
   stop
   start
;;
"start")
   start
;;
"stop")
   stop
;;
*) echo "only accept params start|stop|restart" ;;
esac