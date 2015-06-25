
basedir=$(pwd)

cd ./database-h2-server
nohup ./run.sh &
cd $basedir

cd ./chat-central-server
nohup ./run.sh
cd $basedir

cd ./chat-front-server/bin
nohup ./run.sh
cd $basedir

cd ./chat-node-server/bin
nohup ./run.sh
cd $basedir
