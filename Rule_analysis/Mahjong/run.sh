#"GB 2312"
javac \
   -encoding "GBK" \
   -cp "lib/*" \
   -d bin \
   $(find src -name "*.java")

# java -cp "bin:lib/*" driver.driver
java -cp "bin:lib/*" chengjun.atpg
