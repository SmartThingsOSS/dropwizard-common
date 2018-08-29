Common Dropwizard Modules
=========================

[![codecov](https://codecov.io/gh/SmartThingsOSS/dropwizard-common/branch/master/graph/badge.svg)](https://codecov.io/gh/SmartThingsOSS/dropwizard-common)

This repo is in place to house common dropwizard modules that can be utilized by
dropwizard projects. You are encouraged to use and improve these so that you get
the platform best practices and requirements for "free", and so that issues that
come up can be fixed in one place.

Also the hope is to open source this repo at some once things are fleshed out
further so don't include anything that you wouldn't consider open sourcing.

See each modules documentation for usage details.


Running 1.2.x
=========================

Due to a dependency with Cassandra Core Driver and Guava 19.x there is a conflict between Dropwizard 1.2.x's Guava dependency and those needed by the dropwizard-cassandra module. For projects requiring Cassandra it is recommended you use the 1.0.x releases of Dropwizard and Dropwizard Common. When Datastax releases a final version of their 4.x driver which removes the Guava dependency entirely we will look at using that and merging this branch back into master.