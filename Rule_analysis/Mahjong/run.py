#! /home/venus/anaconda3/bin/python
import os
#"GB 2312", build the program
build_cmd = """javac \
   -encoding "GBK" \
   -cp "lib/*" \
   -d bin \
   $(find src -name "*.java")
"""
os.system(build_cmd)
for i in [1,2,0]:
   run_cmd = 'java -Xmx32g -cp "bin:lib/*" chengjun.atpg {testcase} > run.log  2>&1'.format(testcase = i)
   os.system(run_cmd)
