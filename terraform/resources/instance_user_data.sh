#! /bin/bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

cd /
sudo mkdir external-sorting
cd external-sorting || exit
sudo aws s3 cp s3://aws-external-sorting/application.yml application.yml
sudo aws s3 cp s3://aws-external-sorting/external-sorting-1.0.jar external-sorting-1.0.jar

sudo mkfs -t ext4 /dev/sdh
sudo mkdir -p temp
sudo mount /dev/sdh temp/

sudo yum install java-1.8.0-openjdk -y
sudo java -jar -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.rmi.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Xms3000m -Xmx3000m external-sorting-1.0.jar