jdk_url="http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jdk-8u45-linux-x64.tar.gz"
kenzanmedia_apps="/opt/kenzanmedia/apps"
java_root="${kenzanmedia_apps}/java"
java_home="${java_root}/jdk8"
java_default="${java_root}/default"
java_alternatives="/etc/alternatives/java"

jdk_version="8u45"
tmp_file=/tmp/jdk${jdk_version}.tgz
wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" $jdk_url -O $tmp_file

mkdir -p ${java_home}
tar zxf $tmp_file -C ${java_home} --strip 1

ln -s ${java_home}/bin/java /usr/bin/java
