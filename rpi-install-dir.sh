#!/bin/bash

BUILD_DIR=../pi-gen/stage2/05-brewctrl/files/
#BUILD_DIR=package


make rpi
rm -rf $BUILD_DIR
mkdir -p ${BUILD_DIR}
cp -r brewctrl brewctrl.service cfg.json.dist ${BUILD_DIR}/
cp -r build ${BUILD_DIR}/web



