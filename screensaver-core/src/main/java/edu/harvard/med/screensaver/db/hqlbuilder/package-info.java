/**
 * Contains classes that can be used to build HQL queries. Normally, one could
 * use the Hibernate Criteria API, however, this API <a href="http://opensource.atlassian.com/projects/hibernate/browse/HHH-16?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel&focusedCommentId=35402#action_35402"
 * >does not support multiple, independent joins of the same entity type</a>,
 * which required to implement Screensaver's data tables.
 */
package edu.harvard.med.screensaver.db.hqlbuilder;
