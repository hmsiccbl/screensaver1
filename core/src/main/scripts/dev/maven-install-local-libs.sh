cd `dirname $0`/../../../../local_libs
mvn install:install-file -Dfile=0_hibernate-core-patch-3.6.2.Final.jar -DgroupId=edu.harvard.med.iccbl -DartifactId=0_hibernate-core-patch -Dversion=3.6.2.Final -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=eCommonsAuth.jar -DgroupId=edu.harvard.med -DartifactId=eCommonsAuth -Dversion=5552 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=annotations.jar -DgroupId=util.findbugs -DartifactId=annotations -Dversion=3480 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=joda-time-hibernate-1.2.recompiled.jar -DgroupId=joda-time -DartifactId=joda-time-hibernate-recompiled -Dversion=1.2 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=tomahawk-sandbox-1.1.7-SNAPSHOT.jar -DgroupId=org.apache.myfaces.tomahawk -DartifactId=tomahawk-sandbox -Dversion=1.1.7 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=tomahawk-facelets-taglib.jar -DgroupId=tomahawk -DartifactId=tomahawk-facelets-taglib -Dversion=0.1 -Dpackaging=jar -DgeneratePom=true
