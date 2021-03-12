#!/bin/bash

BUILD_DIR=../pi-gen/stage2/05-brewctrl/files/
#BUILD_DIR=build


make rpi
rm -rf $BUILD_DIR
mkdir -p ${BUILD_DIR}
cp -r brewctrl brewctrl.service web cfg.json.dist ${BUILD_DIR}/


