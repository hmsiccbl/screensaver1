echo "begin;" >drop_all.sql
psql -Ustagepharmacoresponseweb stagepharmacoresponse -c \\d |grep table |nawk '{print "drop table " $3 " cascade;" }'>>drop_all.sql
psql -Ustagepharmacoresponseweb stagepharmacoresponse -c \\d |grep sequence |nawk '{print "drop sequence " $3 " cascade;" }'>>drop_all.sql
echo "commit;" >>drop_all.sql
