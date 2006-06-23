# usage: run.sh <fully qualifed class> <arg>...

export CLASSPATH=classes:`find lib -name '*.jar' -printf ':%p'`
java -Xmx1024m -cp $CLASSPATH "$@"

