```sh
sudo docker run -u root -d -p 8080:8080 -p 50000:50000 -v ~/docker/jenkins/:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock --name=jenkins  --restart=always jenkins/jenkins:lts
```

