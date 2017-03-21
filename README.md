# Capraia
A sample multi-project layout for [Tendermint](https://tendermint.com) applications.

[![Build Status](https://travis-ci.org/afide/capraia.svg?branch=master)](https://travis-ci.org/afide/capraia)
[![Quality Gate](https://sonarqube.com/api/badges/gate?key=com.capraia.afide.capraia:capraia)](https://sonarqube.com/dashboard/index/com.capraia.afide.capraia:capraia)

#### Prerequisites
Gradle runs on all major operating systems and requires only a Java JDK version 8 or higher to be installed.
To check, run `java -version`

#### Get the Sources
```
$ git clone --recursive https://github.com/afide/capraia
```

#### Displays the Project Structure
```
$ ./gradlew projects
```

#### Display Tasks
```
$ ./gradlew tasks
```

#### Build
```
$ ./gradlew build
```

#### Run the Counter Sample Application
```
$ ./gradlew :applications:capraia-counter:run
```

#### Run the Dummy Sample Application
```
$ ./gradlew :applications:capraia-dummy:run
```

#### More Information
Gradle projects are self descriptive. See the [Gradle documentation](https://gradle.org/docs) for more information.
