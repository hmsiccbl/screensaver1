TOP=../..
PKG=${shell cd ${TOP};pwd}
SUITE=doRUnit.R
R=R

all:	inst test

# in beginning of a command in a rule, GNU Make insists that every such line begins with a TAB.

# Install package
inst: 
	cd ${TOP}/..;\
	${R} CMD INSTALL ${PKG}

# Run unit tests
test: 
	export RCMDCHECK=FALSE;\
	cd ${TOP}/tests;\
	${R} --vanilla --slave < ${SUITE}
