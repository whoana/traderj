#!/bin/sh
################################################################################
# SET JAVA_HOME
################################################################################
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home

################################################################################
# SET TRADERJ_HOME
################################################################################
export TRADERJ_HOME=/Users/whoana/DEV/workspaces/myproject/traderj/home

################################################################################
# SET TRADERJ_OPT
################################################################################
export TRADERJ_OPT="${TRADERJ_OPT} -Dtraderj.home=${TRADERJ_HOME}"
export TRADERJ_OPT="${TRADERJ_OPT} -Dlogback.configurationFile=${TRADERJ_HOME}/config/logback.xml"
export TRADERJ_OPT="${TRADERJ_OPT} -Dtraderj.log.dir=${TRADERJ_HOME}/log"

################################################################################
# SET VM_OPT
################################################################################
export VM_OPT="-Xmx1G -Xms1G"
export VM_OPT="${VM_OPT} -server"
echo "VM_OPT:${VM_OPT}"


################################################################################
# SET CLASSPATH
################################################################################
export CLASSPATH=${TRADERJ_HOME}/lib/traderj-1.0-SNAPSHOT-jar-with-dependencies.jar
echo "CLASSPATH:${CLASSPATH}"

RUN_OPT=$1
echo "RUN_OPT:${RUN_OPT}"

echo "TRADERJ_OPT:${TRADERJ_OPT}"
if [ "$RUN_OPT" = "-f" ]; then
    echo "forground start..."
    ${JAVA_HOME}/bin/java ${VM_OPT} ${TRADERJ_OPT} -classpath ${CLASSPATH} com.smthe.money.managers.BotManager
else
    echo "background start..."
    nohup ${JAVA_HOME}/bin/java ${VM_OPT} ${TRADERJ_OPT} -classpath ${CLASSPATH} com.smthe.money.managers.BotManager 1>/dev/null 2>&1 &
fi
