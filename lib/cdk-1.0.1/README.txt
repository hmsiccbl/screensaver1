NOTE: CDK gloms class files from other Java open source libraries directly in to their distribution jars,
setting the stage for confusing library version conflict problems!

The cdk-1.0.1-custom.jar in this directory was custom built by the Screensaver team to avoid problems involving
version conflicts with the commons-cli library. Screensaver needs commons-cli >= 1.1, as it depends on a bug fix
introduced in 1.1, and CDK is still packaging commons-cli-1.0. We constructed a cdk-1.0.1-custom.jar to be identical
with their released cdk-1.0.1.jar, except for the exclusion of commons-cli classes. Steps to construct the jar were
as follows:

1. Download the source distribution from the CDK website.
2. Build cdk-1.0.1.jar via Ant rule dist-large.
3. Run `jar xf cdk-1.0.1.jar` into an empty directory to extract the contents of the jar.
4. Execute `rm -rf org/apache/commons/` to remove the org/apache/commons directory, which contains the commons-cli
   classes (and nothing else).
5. Run `jar cf ../cdk-1.0.1-custom.jar .` to build the custom jar that is identical to the original jar, except
   without the commons-cli classes.
