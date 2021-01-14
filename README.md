# Chemical Search Service

[![Build Status](https://travis-ci.com/ArqiSoft/crystal-file-search-service.svg?branch=master)](https://travis-ci.com/ArqiSoft/crystal-file-search-service)

## About

Chemical Search is a demo application to evaluate chemical structure searches performance running Bingo API with ElasticSearch (<https://github.com/epam/Indigo/tree/master/api/plugins/bingo-elastic/java>). This application has been built for research purposes only and can be stopped any time.

Publicly available SureChEMBL dataset (<ftp://ftp.ebi.ac.uk/pub/databases/chembl/SureChEMBL/data//>) is preloaded into ElasticSearch and current index contains more than 20 millions compounds.

The application hosted in AWS environment on a separate EC2 instance with ElasticSearch 7 running in Docker but some other configurations may be applied later.

## System Requirements

Java 1.8, Maven 3.x
Optional: docker, docker-compose

## Local Build Setup

```bash
# build
mvn clean package

# run as standalone application
mvn spring-boot:run
```

## Create and start docker image

1. Use `docker-compose build` command to build the docker image.
2. Use `docker-compose -f docker-compose.cluster.yml up` command to launch the docker image.
