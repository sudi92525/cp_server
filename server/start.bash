#! /bin/sh

#启动方法
start(){

        java -Xms5000M -Xmx5000M -Xmn500M -XX:SurvivorRatio=1 -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC  -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:/mnt/logs/xnsccp/gc.log -jar xnsccp_server.jar  4567 &
}
#停止方法
stop(){
        ps -ef|grep xnsccp_server|awk '{print $2}'|while read pid
        do
           kill -15 $pid
        done
}

case "$1" in
start)
  start
  ;;
stop)
  stop
  ;;
restart)
  stop
  start
  ;;
*)
  printf 'Usage: %s {start|stop|restart}\n' "$prog"
  exit 1
  ;;
esac
