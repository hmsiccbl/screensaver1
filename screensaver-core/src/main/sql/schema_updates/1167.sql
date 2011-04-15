alter table copy_info drop volume;
alter table copy_info add volume numeric(19, 2);
alter table copy_info alter volume set not null;