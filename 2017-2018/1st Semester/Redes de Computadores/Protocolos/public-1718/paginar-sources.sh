#!/bin/bash

usage() {
    echo "usage: paginar-sources <zip>"
    exit 1
}

if [ "$#" -ne 1 ]; then
   usage
fi

function cleanup {
	 rm -f $TEMP_DIR/trab2.*
	 rmdir $TEMP_DIR
}

trap usage ERR
trap cleanup EXIT

ZIP=$1

CURR_DIR=`pwd`
TEMP_DIR=$CURR_DIR/.temp
mkdir -p $TEMP_DIR

echo "Generating printable sources in pdf from: " $ZIP
cp -f "$ZIP" "$TEMP_DIR/trab2.zip"
docker pull smduarte/rc17-t2-pdf > /dev/null
docker run --rm -ti -v "${TEMP_DIR}:/src" smduarte/rc17-t2-pdf > /dev/null
mv $TEMP_DIR/sources-*.pdf .

echo "===================================="
echo "PAGINATED PDF GENERATED WITH SUCCESS"
ls  sources-*.pdf
echo "===================================="


