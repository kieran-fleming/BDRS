#!/bin/bash

usage()
{
cat << EOF
usage: $0 options

This script takes a directory with the target data to overlay the war file.

OPTIONS:
   -t      target WAR file (optional, will use /target/bdrs-core.war by default) Absolute or relative path
   -s      source directory. This should be the the base directory of the data to overlay the WAR file with. Absolute or relative path

Example:
$0 -s ~/workspace/GR205-DIDMS/deployment/external_stage/war_file/
$0 -t target/bdrs-core.war -s ~/workspace/GR205-DIDMS/deployment/external_stage/war_file/
EOF
}

TARGET="target/bdrs-core.war"
SOURCE=

while getopts “ht::s:” OPTION
do
    case $OPTION in
        h)
            usage
            exit 1
            ;;
        t)
            TARGET=$OPTARG
            ;;
        s)
            SOURCE=$OPTARG
        ;;
    esac
done

if [[ -z $TARGET ]] || [[ -z $SOURCE ]]
then
    usage
    exit 1
fi

if [[ ! $TARGET == \/* ]]
then
    # Convert to absolute path
    TARGET=$PWD"/"$TARGET
fi

if [[ ! -f $TARGET ]]
then
    echo "Target WAR file" \"$TARGET\" "does not exist or is not a file"
    exit 1
fi

if [[ ! -d $SOURCE ]]
then
    echo "Source directory" \"$SOURCE\" "does not exist"
    exit 1
fi

if [[ ! -d $SOURCE"/WEB-INF" ]]
then
    echo "Expected to find the WEB-INF directory in the source dir. Are you sure this is the right directory?"
    read -p "Do you want to continue (y/n)?: " CHOICE
    if [ "$CHOICE" = "n" ]
    then
        echo "WAR file has not been overlayed"
        exit 0
    fi
fi

pushd $SOURCE

zip -r $TARGET . -x \*/.* -x \*.svn*

popd

echo "WAR file" \"$TARGET\" "successfully overlayed with data from" \"$SOURCE\"

