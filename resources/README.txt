A Note Regarding the screensaver.properties File:

The screensaver.properties file contains properties used at both build-time and
run-time. You will want to soft-link from one of the screensaver.properties.* files to
screensaver.properties for local deployment and running programs out of Eclipse. For
deployment, you can set the Ant property ${screensaver.properties.file} to whatever you
want, and the deployed system will use that value at runtime. (See
flotsam+jetsam/orchestra-bin/ss-deploy{,-dev} for examples.)

You can also define any Ant properties you want to in this file.
