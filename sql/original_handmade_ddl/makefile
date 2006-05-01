# screensaver/sql/makefile
# by john sullivan, 2006.04

.PHONY: all help recreate_schema drop_schema query_drop_schema
.PHONY: create_schema

PSQL = psql
POSTGRESQL_DATABASE = devscreensaver
POSTGRESQL_USER = js163

default : help

help :
	@echo
	@echo "	available targets:"
	@echo "help			displays this message"
	@echo "recreate_schema		runs the following targets: drop_schema create_schema"
	@echo "drop_schema		drop all the tables from the database"
	@echo "create_schema		create all the tables and indexes"
	@echo "psql.%.sql		loads file %.sql into the current database using $(PSQL)"
	@echo
	@echo "your database is:		$(POSTGRESQL_DATABASE)"
	@echo "your database user is:		$(POSTGRESQL_USER)"
	@echo
	@echo "you can choose a different database or user like so:"
	@echo "	make <target> POSTGRESQL_DATABASE=<database> POSTGRESQL_USER=<user>"
	@echo

recreate_schema : drop_schema create_schema

drop_schema : query_drop_schema psql.drop_schema.sql

query_drop_schema :
	@echo you are about to delete the PostgreSQL database $(POSTGRESQL_DATABASE).
	@if read -t 10 -p "are you sure want to continue (y/n)? " reply ; \
	then \
	  [[ $${reply:0:1} == "Y" || $${reply:0:1} == "y" ]] ; \
	else \
	  echo ; false ; \
	fi

create_schema : psql.create_schema.sql

psql.%.sql : %.sql ; $(PSQL) -f $< $(POSTGRESQL_DATABASE) $(POSTGRESQL_USER)
