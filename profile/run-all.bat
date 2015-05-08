@echo off
pushd .\database-h2-server
start run.bat
popd

pushd .\chat-central-server
start run.bat
popd

pushd .\chat-front-server\bin
start run.bat
popd

pushd .\chat-node-server\bin
start run.bat
popd

exit