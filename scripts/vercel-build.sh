#!/usr/bin/env bash
set -euo pipefail

java_major_version() {
  java -version 2>&1 | awk -F '[".]' '/version/ { print $2; exit }'
}

ensure_java17() {
  if command -v java >/dev/null 2>&1 && [ "$(java_major_version)" -ge 17 ] 2>/dev/null; then
    return
  fi

  if command -v dnf >/dev/null 2>&1; then
    dnf install -y java-17-amazon-corretto-devel
  elif command -v yum >/dev/null 2>&1; then
    yum install -y java-17-amazon-corretto-devel
  else
    echo "Java 17+ is required, but no supported package manager was found to install it." >&2
    exit 1
  fi
}

ensure_java17

if [ -z "${JAVA_HOME:-}" ]; then
  JAVA_BIN="$(readlink -f "$(command -v java)")"
  export JAVA_HOME="$(dirname "$(dirname "$JAVA_BIN")")"
fi

export PATH="$JAVA_HOME/bin:$PATH"
export GRADLE_OPTS="${GRADLE_OPTS:-} -Dorg.gradle.java.home=$JAVA_HOME"

echo "Using Java: $(java -version 2>&1 | head -n 1)"
echo "JAVA_HOME=$JAVA_HOME"

chmod +x gradlew
./gradlew :teavm:build --no-daemon
