hibernate-properties-ResultValueType.xml

   A hack that allows us to specify the 'lazy="extra"', as XDoclet does not
   support/allow the 'extra' value.  XDoclet does allow us to merge in
   additional Hibernate property definitions from an external file, which
   explains this existence of this file.  Instead of defining the
   ResultValueType.resultValues collection in our Java code, we define it in
   this file and have XDoclet merge it into the generated the
   ResultValueType.hbm.xml file.  See build.xml.

ResultValueType.replace.properties

   This is part of a hack that creates custom db table indexes.  This
   following 'replace' statement does this.  Uses's Hibernate's
   <database-object> mechanism for defining custom create/drop SQL statements
   (http://www.hibernate.org/hib_docs/v3/reference/en/html/mapping.html#mapping-database-object),
   which is not supported by XDoclet, forcing us to do the dirty work of
   merging this definition into our hibernate-properties-*.xml file(s).  See
   build.xml.
