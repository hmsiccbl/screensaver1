# usage: run.sh <fully qualifed class> <arg>...

export CLASSPATH=classes:`find lib -name '*.jar' -printf ':%p'`
java -Xmx800m -cp $CLASSPATH "$@"

