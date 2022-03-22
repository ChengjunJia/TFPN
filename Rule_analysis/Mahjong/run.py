#! /home/venus/anaconda3/bin/python
import os
from multiprocessing import Pool
#"GB 2312", build the program
build_cmd = """javac \
   -encoding "GBK" \
   -cp "lib/*" \
   -d bin \
   $(find src -name "*.java")
"""
os.system(build_cmd)

def system_run_cmd(cmd):
   os.system(cmd)

p = Pool(processes = 4)

for i in [1, 2, 0]: #
   # run_cmd = 'java -Xmx5g -cp "bin:lib/*" chengjun.atpg {testcase} > run_atpg.log  2>&1'.format(testcase = i)
   # p.apply_async(system_run_cmd, (run_cmd,))

   # run_cmd = 'java -Xmx5g -cp "bin:lib/*" chengjun.atpgOnlyEndhost {testcase} > run_atpgOnlyEndhost.log  2>&1'.format(testcase = i)
   # p.apply_async(system_run_cmd, (run_cmd,))

   run_cmd = 'java -Xmx5g -cp "bin:lib/*" chengjun.increment {testcase} > run_atpg_increment.log  2>&1'.format(testcase = i)
   p.apply_async(system_run_cmd, (run_cmd,))
   
   # run_cmd = 'java -Xmx5g -cp "bin:lib/*" chengjun.atpgOnlyEndhostCombineDst {testcase} > run_atpgOnlyEndhostCombine.log  2>&1'.format(testcase = i)
   # p.apply_async(system_run_cmd, (run_cmd,))

p.close()
p.join()
print("Finish the running")