docker rm $(docker ps -a -f status=exited -q)
docker rmi -f $(docker images -a -q)
