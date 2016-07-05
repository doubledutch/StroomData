#!/bin/bash

mkdir stroom
cp -Rf tools ./stroom/
cp LICENSE ./stroom/
cp README.md ./stroom/
cp data_set* ./stroom/
mfile=`ls reference/build/libs/multihost-* | sort -n | tail -n 1`
cp $mfile ./stroom/
mfile=`ls reference/build/libs/client-* | sort -n | tail -n 1`
cp $mfile ./stroom/
cp -Rf reference/build/docs/clientapi ./stroom/

version=`cat reference/src/main/resources/version.properties | grep BUILD_VERSION | sed s/.*=//`
tar -zcvf stroom-$version.tgz stroom
rm -Rf stroom
