#! /bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
DAEMON="/usr/lib/jvm/java-6-openjdk-i386/jre/bin/java"
DESC=ewhine-search

min_heap_size="64m"
max_heap_size="128m"

serverdir=/home/adminer/deploy/ewhine_search
serverjar=$serverdir/server.jar
server_temp=$serverdir/tmp
server_logs=$serverdir/logs
server_name="ewhine_search"
PIDFILE=$server_temp/$server_name.pid
USER=adminer
DB_ENV=production

test -x $DAEMON || exit 0
test -x $serverdir || exit 0

set -e

ARGS="-server -Xms$min_heap_size -Xmx$max_heap_size -XX:PermSize=128m -Xloggc:$server_logs/gc.log -XX:+PrintGCTimeStamps -XX:-PrintGCDetails -Ddb.env=produc
tion -Djava.awt.headless=true -jar $serverjar"

case "$1" in
start)
echo -n "Starting $DESC: "
touch $PIDFILE
chown "$USER" $PIDFILE
if start-stop-daemon --start --chdir $serverdir --quiet --make-pidfile --pidfile $PIDFILE --chuid "$USER" --background --exec $DAEMON -- $ARGS
then
echo "$server_name."
else
echo "failed"
fi
;;
stop)
echo -n "Stopping $DESC: "
if start-stop-daemon --stop --retry 10 --quiet --pidfile $PIDFILE
then
echo "$server_name."
else
echo "failed"
fi
rm -f $PIDFILE
;;

restart|force-reload)
${0} stop
${0} start
;;
*)
echo "Usage: /etc/init.d/$server_name {start|stop|restart|force-reload}" >&2
exit 1
;;
esac

exit 0