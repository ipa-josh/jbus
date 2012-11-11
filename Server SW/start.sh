#!/bin/bash

#create emlog with 512kB for std/error
mknod /tmp/jhalog1 c 241 512
mknod /tmp/jhalog2 c 241 512

#start jha
java -jar JHA.jar 1>/tmp/jhalog1 2>/tmp/jhalog2