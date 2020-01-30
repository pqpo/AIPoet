#!/bin/bash

for fff in `ls data/raw-data/*.json`
do
target="data/${fff#data/raw-data/}"
echo "$fff ---> $target"
cconv -f utf8-tw  -t UTF8-CN $fff > $target
done