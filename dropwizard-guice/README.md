Dropwizard Guice
================

Guice integration with dropwizard. This library is fairly opinionated in order to remain fast, simple, and easy to use.

Currently the main limitation to be aware of is that any object being registered with dropwizard must be a singleton.
This limitation may be removed in the future, but for now this makes it much easier to avoid odd HK2 integration.

NOTE: that this project is inspired by [the hubspot dropwizard guice](https://github.com/HubSpot/dropwizard-guice)
project, however it does a couple key things differently. In this library we avoid classpath scanning to pick up
resources, only deal with singletons being registered into dropwizard, and we allow for eager singletons out of the box.
