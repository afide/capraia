# Capraia
A sample multi-project layout for [Tendermint](https://tendermint.com) applications.

[![Build Status](https://travis-ci.org/afide/capraia.svg?branch=master)](https://travis-ci.org/afide/capraia)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.capraia.afide.capraia:capraia&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.capraia.afide.capraia:capraia)

#### Prerequisites
This project requires a Java JDK version 8 or higher to be installed.
To check, run `javac -version`

#### Get the Sources
```
$ git clone --recursive https://github.com/afide/capraia
$ cd ./capraia
$ git submodule foreach -q --recursive 'branch="$(git config -f $toplevel/.gitmodules submodule.$name.branch)"; git checkout $branch'
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
