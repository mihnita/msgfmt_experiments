#!/bin/bash

./clean.sh
tsc -b --pretty
node out/messageformat_test.js
