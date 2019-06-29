#!/bin/bash

usage() {
    echo "usage: t1_test pdf <zip>"
    echo "usage: t1_test [1|2|3|4|5] [<jar>|SW]"
    exit 1
}

if [ "$#" -ne 2 ]; then
	usage
fi

function cleanup {
	rm -f $TEMP_DIR/trab1.*
	rmdir $TEMP_DIR
}

trap usage ERR
trap cleanup EXIT

JAR=$2
ZIP=$2
TEST=$1

CURR_DIR=`pwd`
TEMP_DIR=$CURR_DIR/.temp
mkdir -p $TEMP_DIR

if [ $1 = "pdf" ]; then
    echo "Generating source pdf from: " $ZIP
	cp -f "$ZIP" "$TEMP_DIR/trab1.zip"
	docker pull smduarte/rc17-t1-base
	docker run --rm -ti -v "${TEMP_DIR}:/jar" smduarte/rc17-t1-base
	mv $TEMP_DIR/sources-*.pdf .
	ls -al sources-*.pdf
else
	docker pull smduarte/rc17-t1-test${TEST}
    if [ $JAR = "SW" ]; then
        echo "Testing cenario$TEST with embedded S/W client";
		docker pull smduarte/rc17-t1-test${TEST}
		docker run --rm --privileged -t smduarte/rc17-t1-test${TEST}
    else
        echo "Testing cenario$TEST with jar: " $JAR;
		cp -f "$JAR" "$TEMP_DIR/trab1.jar"
		docker pull smduarte/rc17-t1-test${TEST}
		docker run --rm --privileged -t -v "${TEMP_DIR}:/jar" smduarte/rc17-t1-test${TEST}
    fi	
fi

