#!/bin/bash

function exitCall() {
    printf "%s" "$1"
    exit 0
}

function printHelp() {
  printf "Usage:
\e[1m\e[4m./KwikUpdater.sh\e[0m
Get the default repository https://bitbucket.org/pjtr/kwik.git\n
\e[1m\e[4m./KwikUpdater.sh <Kwik_root_folder>\e[0m
Changes the source files in a given Kwik project.\n
\e[1m\e[4m./KwikUpdater.sh git <git_repository_link>\e[0m
clones a user speciefied git repository.\n
\e[1m\e[4m./KwikUpdater.sh <?|help>\e[0m
Print this help dialog.\n"
}

# Save the current path
path=$(pwd)


# Get or open repository
if [ "$1" == "?" ] || [ "$1" == "help" ]; then
  printHelp
  exit 0
else
  if  [ "$1" == "git" ]; then
    if [ "$2" == "" ]; then
      cd /tmp || exit
      rm -rf kwik
      git clone "$2"
      cd kwik || exit
    fi
  else
    if [ "$1" == "" ]; then
      cd /tmp || exit
      rm -rf kwik
      git clone https://bitbucket.org/pjtr/kwik.git
      cd kwik || exit
    else
      cd "${1}" || exit
    fi
  fi
fi

#Replace the member access rights for the necessecary parts of kwik
sed -i -e 's/^\s*useJUnitPlatform()//g' build.gradle
## If sed is no match... exitCall
sed -i -e 's/^\s*private volatile Status connectionState/    public volatile Status connectionState/g' src/main/java/net/luminis/quic/QuicConnection.java
sed -i -e 's/^\s*public void connect(int/    public void connect(int/g' src/main/java/net/luminis/quic/QuicConnection.java

# Build Kwik
cd /tmp/kwik || exit
gradle build

# Move quic to the clojure project
cp build/libs/quic.jar "${path}/libs/quic.jar"
printf "Kwik lib updated"
