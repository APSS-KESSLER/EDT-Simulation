#!/bin/bash
c++ -g -Wall -Wextra -O3 -std=c++0x -Iinclude -c -o Gravity.o src/Gravity.cpp
g++ -g  -o Gravity Gravity.o -Lsrc -lGeographic 
./Gravity
