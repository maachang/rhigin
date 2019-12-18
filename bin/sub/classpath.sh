#!/bin/bash
SCRIPT_DIR=$(cd $(dirname $0); pwd)
for DIR in $*; do
    if [ "x`ls $DIR`" != "x" ]; then
        for JAR in ` ls $DIR`; do
            if [ -d "${DIR}/${JAR}" ]; then
                JAR=` sh ${SCRIPT_DIR}/classpath.sh "${DIR}/${JAR}"`
                if [ "x$CLASSPATH" = "x" ]; then
                    CLASSPATH=${JAR}
                else
                    CLASSPATH=$CLASSPATH:${JAR}
                fi
            elif [ -f "${DIR}/${JAR}" ]; then
                if [ "x$CLASSPATH" = "x" ]; then
                    CLASSPATH=${DIR}/${JAR}
                else
                    CLASSPATH=$CLASSPATH:${DIR}/${JAR}
                fi
            fi
        done
    fi
done

echo $CLASSPATH
