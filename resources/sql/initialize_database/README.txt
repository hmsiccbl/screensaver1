how database initialization works:

1. the files in this directory with extension ".sql" are processed, in alphabetical order.
2. each file is read, line by line.
3. if the line ends with a semicolon (';'), then the line is sent to JDBC as SQL.
4. otherwise the line is ignored.
5. this means that multiline SQL statements (like COPY commands) will break the initialization
   mechanism!
6. if any errors are encountered, then initialization abruptly halts. any uncommitted changes
   will be discarded. however, please note that the individual SQL files may contain
   commit statements, which will presumably commit the contents of that file, so an error
   encountered in a later file will presumably not rollback statements from an earlier
   initialization file. but, this has not been tested.

please prefix initialization files 01_, 02_, etc. if you want or need to change the order,
just rename the files.

my apologies for dumping the sql directory wholesale into resources! i can look into
improving this situation; it was a minor hassle getting this to work both within eclipse,
and within a web context.

the contents of this file, as well as the file itself, are GPL baby!