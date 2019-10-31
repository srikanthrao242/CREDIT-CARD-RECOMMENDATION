#!/usr/bin/env bash


echo "********************** starting CREDIT CARD RECOMMENDATIONS***************************************"
sbt clean
echo "********************** starting sbt compile***************************************"
sbt compile
echo "********************** starting sbt test***************************************"
sbt test
echo "********************** starting sbt compile***************************************"
sbt run
echo echo "********************** RUNNING CREDIT CARD RECOMMENDATIONS***************************************"

