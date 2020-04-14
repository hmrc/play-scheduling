# play-scheduling

[![Build Status](https://travis-ci.org/hmrc/play-scheduling.svg?branch=master)](https://travis-ci.org/hmrc/play-scheduling) [ ![Download](https://api.bintray.com/packages/hmrc/releases/play-scheduling/images/download.svg) ](https://bintray.com/hmrc/releases/play-scheduling/_latestVersion)

Helpers for scheduling jobs from Play framework on the Tax Platform

## Play 2.5
A given function can be run periodically in the background by mixing in the ```RunningOfScheduledJobs```  trait to the service's ```Global```

## Play 2.6
A given function can be run periodically in the background by mixing in the ```RunningOfScheduledJobs``` trait with a singleton class created using dependency injection.