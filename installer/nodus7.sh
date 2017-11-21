#! /bin/sh
NODUS7_HOME=%INSTALL_PATH

LIBDIR=${NODUS7_HOME}/lib/*
PLAFSDIR=${NODUS7_HOME}/plafs/*
JDBCDIR=${NODUS7_HOME}/jdbcDrivers/*
JVMARGS="-Xms2000m -Xmx6000m"

NODUSCP="${NODUS7_HOME}/nodus7.jar:$LIBDIR:$PLAFSDIR:$JDBCDIR:$NODUS7_HOME"

java -cp $NODUSCP $JVMARGS -DNODUS_HOME=$NODUS7_HOME edu.uclouvain.core.nodus.Nodus7 "$@"
