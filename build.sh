#!/bin/sh
# Java 17 build without Maven. Run from any directory: sh /path/to/Lab6/build.sh
set -eu
cd "$(dirname "$0")"
version=2.25.3
mkdir -p lib dist
for component in api core; do
    dependency="lib/log4j-$component-$version.jar"
    if [ ! -s "$dependency" ]; then
        curl -fL --connect-timeout 20 --retry 2 -o "$dependency.download" \
          "https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-$component/$version/log4j-$component-$version.jar"
        mv "$dependency.download" "$dependency"
    fi
done
build_dir=$(mktemp -d "${TMPDIR:-/tmp}/lab6-build.XXXXXX")
mkdir -p "$build_dir/common" "$build_dir/client" "$build_dir/server"
find common/src/main/java -name '*.java' > "$build_dir/common-sources"
find client/src/main/java -name '*.java' > "$build_dir/client-sources"
find server/src/main/java -name '*.java' > "$build_dir/server-sources"
javac --release 17 -encoding UTF-8 -d "$build_dir/common" @"$build_dir/common-sources"
javac --release 17 -encoding UTF-8 -cp "$build_dir/common" -d "$build_dir/client" @"$build_dir/client-sources"
javac --release 17 -encoding UTF-8 -cp "$build_dir/common:lib/*" -d "$build_dir/server" @"$build_dir/server-sources"
jar --create --file dist/Lab6-client.jar --main-class itmo.lab6.client.ClientMain \
    -C "$build_dir/common" . -C "$build_dir/client" .
# Fat server JAR: dependency resources (including the Log4j plugin cache) are preserved.
project_dir=$(pwd)
(cd "$build_dir/server" && jar xf "$project_dir/lib/log4j-api-$version.jar" && jar xf "$project_dir/lib/log4j-core-$version.jar")
jar --create --file dist/Lab6-server.jar --main-class itmo.lab6.server.ServerMain \
    -C "$build_dir/common" . -C "$build_dir/server" . -C server/src/main/resources .
printf 'Built dist/Lab6-client.jar and dist/Lab6-server.jar (Java 17).\n'
