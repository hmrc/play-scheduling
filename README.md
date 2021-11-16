# play-scheduling

[![Build Status](https://travis-ci.org/hmrc/play-scheduling.svg?branch=master)](https://travis-ci.org/hmrc/play-scheduling) [ ![Download](https://api.bintray.com/packages/hmrc/releases/play-scheduling/images/download.svg) ](https://bintray.com/hmrc/releases/play-scheduling/_latestVersion)

Helpers for scheduling jobs from Play framework on the Tax Platform

## Play 2.5
A given function can be run periodically in the background by mixing in the ```RunningOfScheduledJobs```  trait to the service's ```Global```

## Play 2.6 and Play 2.7
A given function can be run periodically in the background by mixing in the ```RunningOfScheduledJobs``` trait with a singleton class created using dependency injection.

## Play 2.8
Note that play-scheduling has intentionally **not** been migrated to Play 2.8 as the logic is mostly in one class. You can use mongo-lock with akka scheduling as an alternative option.
