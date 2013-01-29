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
server_temp=$serverdir/tmp
server_logs=$serverdir/logs
server_name="ewhine_search"
cd $serverdir


start()
{
  nohup java -Xms$min_heap_size -Xmx$max_heap_size -XX:PermSize=128m -Xloggc:$server_logs/gc.log -XX:+PrintGCTimeStamps -XX:-PrintGCDetails -Den -Dfile.encoding=UTF-8 -jar $serverjar  >> $server_logs/server.log 2>&1 &
  echo $! > $server_temp/$server_name.pid
  echo "server started!"
}
stop()
{
  kill  `cat $server_temp/$server_name.pid`
  echo "server stopped!"
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