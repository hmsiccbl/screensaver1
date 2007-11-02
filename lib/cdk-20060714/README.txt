HACK ALERT!
Original jar file name is cdk-20060714.jar, but we want to ensure that when
generating a classpath with alphabetically-ordered jars (as some of our
command-line utility scripts do), that this jar comes after
jakarta-cli-1.1.jar, since cdk-20060714.jar contains the jakarta-cli-1.0 code
which has a bug in it. (RT #103907)
