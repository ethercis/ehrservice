#!/usr/bin/env bash

#create a directory and use it to clone
#the git repo used to display build data and test reports
git init travis-reports
cd travis-reports/
git remote add origin https://${GHTOKEN}@github.com/ecisreportsarikan/ecisreportsarikan.github.io.git > /dev/null 2>&1

#use a shallow clone to minimise traffic
#TODO: trigger a script to archive contents of this repo and reset
#contents to a single entry
git pull --depth=1 origin master

cd .. #climb to parent dir where the git repo that triggerred this build resides
#update html file with data from this build
export COMMIT_ID=$(git rev-parse HEAD)
export COMMIT_SHORT=${COMMIT_ID:0:8}
export BUILD_DATE_TIME=$(git rev-parse HEAD | git show -s --format=%ci)

sed -i 's^<!--template-->^<!--template-->\n<a href="https://github.com/serefarikan/ehrservice/commit/'"$COMMIT_ID"'">'"$COMMIT_SHORT"' (CORE)</a><span> :  ('"$BUILD_DATE_TIME"')</span> <a href="site_'"$COMMIT_SHORT"'/surefire-report.html">Surefire test reports</a> <a href="https://travis-ci.org/serefarikan/ehrservice/builds/'"$TRAVIS_BUILD_ID"'">Travis CI build</a> <a href="https://api.travis-ci.org/v3/job/'"$TRAVIS_JOB_ID"'/log.txt">Travis CI build log(raw)</a><br>^g' travis-reports/index.html

cd travis-reports/ # descend into temp git repo dir so that you can push to reports github page
cp ../target/site -r ./site_$COMMIT_SHORT
git add -A
git config user.email serefarikan@gmail.com
git config user.name ecisreportsarikan
git commit -m"build ehrservice"
git push --set-upstream origin master	
