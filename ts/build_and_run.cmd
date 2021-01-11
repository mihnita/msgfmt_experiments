@echo off

call clean.cmd
call tsc -b --pretty
node out\messageformat_test.js
