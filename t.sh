#/bin/bash

set -e

if [ $# -gt 0 ]; then
    if [ ! -f tests/"$1".xml ]; then
        echo "file tests/$1.xml does not exist"
        exit 1
    else
        cd tests &&
        if [ $# -eq 2 ] && [ "$2" == "-d" ]; then
          ant -Dmy.tasks.debug=on -f "$1".xml
        else
          ant -f "$1".xml
        fi
    fi
else
    cd tests &&
    for xml in *.xml; do
        ant -f "$xml"
    done
fi
