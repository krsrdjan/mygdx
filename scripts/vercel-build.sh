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
# Clean teavm output so a partial Vercel build cache cannot leave index.html without app.js.
./gradlew :teavm:clean :teavm:build --no-daemon

SITE_DIR="teavm/build/dist/site"
if [ ! -f "$SITE_DIR/app.js" ]; then
  echo "ERROR: Missing $SITE_DIR/app.js after TeaVM build." >&2
  find teavm/build/dist -type f 2>/dev/null | head -50 >&2 || true
  exit 1
fi

echo "Web build ready ($(find "$SITE_DIR" -type f | wc -l | tr -d ' ') files in $SITE_DIR)"
