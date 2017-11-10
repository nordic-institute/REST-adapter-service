#!/usr/bin/env bash
# builds rpm packages in docker container "rest-adapter-rpm"
docker run --rm -v $PWD/.:/workspace -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro -u $(id -u):$(id -g) --tmpfs /docker-temp -e _JAVA_OPTIONS=-Duser.home=/docker-temp/docker-build-home rest-adapter-rpm
