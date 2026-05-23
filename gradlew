#!/usr/bin/env sh

# Executavel de inicializacao do Gradle para ambientes Linux (GitHub Actions)
DIRNAME=$(dirname "$0")
if [ -z "$DIRNAME" ]; then
    DIRNAME="."
fi
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$DIRNAME" && pwd)

# Localiza o Java instalado no servidor do GitHub
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVACMD="$JAVA_HOME/bin/java"
    fi
fi

if [ -z "$JAVACMD" ] ; then
    JAVACMD="java"
fi

# Executa o wrapper baixando as dependencias do Lastfom Launcher
exec "$JAVACMD" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
