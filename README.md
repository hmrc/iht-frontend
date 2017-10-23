
## Inheritance Tax Frontend Service

[![Build Status](https://travis-ci.org/hmrc/iht-frontend.svg?branch=master)](https://travis-ci.org/hmrc/iht-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/iht-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/iht-frontend/_latestVersion)

## Requirements

This is a scala based application using the [Play framework](https://playframework.com/) and html and javascript. You will therefore need to have a JRE installed and setup.

## Authentication

The user logs in via the [Government Gateway](http://www.gateway.gov.uk/) service.

## How to run the service

You will need to clone the project first then navigate to the main folder and run the following sbt command ```sbt "run 9070"```. You may need to add the '-mem 4096' switch after sbt so the command would look like ```sbt -mem 4048 "run 9070"```.

You will also need the [IHT Microservice](https://github.com/hmrc/iht) to run on port 9071 in the same way as above.

 **URL**

  `/your-tax-calculator-frontend/calculator`

 API
 ---

 The frontend additionally exposes the following service:

 | *Task* | *Supported Methods* | *Description* |
 |--------|----|----|
 | ```/your-tax-calculator-frontend/version-check``` | POST | Validates the mobile application version [More...](docs/version-check.md) |
 | ```/tax-estimator/tax-credit-renewals/:os``` | GET | Return the tax-credit-renwals JS app [More...](docs/tax-credit-renewals.md) |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
