#!/bin/bash
set -e

echo "Running Script"

projectKey = "SonarCubeTest"
projectName = "SonarCubeTest"

while getopts :s:u:p:n:v: flag
do
    case "${flag}" in
        s) server=${OPTARG};; 
        u) user=${OPTARG};; 
        p) password=${OPTARG};; 
        n) number=${OPTARG};;
        v) version=${OPTARG};;
        \?) echo "Invalid option: -$OPTARG" >&2;; 
    esac
done

echo "Server $server";
echo "User $user";
echo "Password $password"
echo "Number $number"

#Not needed for now
#cd project
#/v:"%build.vcs.number%" \

#If no PR number provided
if [ -z "$number" ]; then
    echo "Iz empty"
    dotnet-sonarscanner begin \
    /k:"$projectKey" \
    /n:"$projectName" \
    /d:sonar.host.url="$server" \
    /d:sonar.login="$user" \
    /d:sonar.password="$password" \
    /d:sonar.pullrequest.key="$number" \
    /d:sonar.cs.opencover.reportsPaths="**/coverage.opencover.xml"
else
    echo "Iz NOT empty"
    dotnet-sonarscanner begin \
    /k:"$projectKey" \
    /n:"$projectName" \
    /d:sonar.host.url="$server" \
    /d:sonar.login="$user" \
    /d:sonar.password="$password" \
    /d:sonar.pullrequest.key="$number" \
    /d:sonar.cs.opencover.reportsPaths="**/coverage.opencover.xml" \
    /d:sonar.pullrequest.branch="pull/$number" \
    /d:sonar.pullrequest.base="master"
fi

# dotnet test -v n TCSonarCube.sln --filter 'FullyQualifiedName~Test.Unit' -p:CollectCoverage=true -p:CoverletOutputFormat=opencover%2cteamcity --results-directory "testresults"
# dotnet-sonarscanner end /d:sonar.login="$user" /d:sonar.password="$password"
