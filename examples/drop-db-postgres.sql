-- To avoid the situation that you need to be superuser to drop the
-- database, instead throw away all of the tables.

drop table bug_report_message;
drop table bug_reporter;
drop table bug;
drop table committer;
drop table group_user;
drop table group_privilege;
drop table groups cascade;
drop table feature;
drop table commits;
drop table mailinglist cascade;
drop table file_metadata cascade;
drop table mailmessage cascade;
drop table measurement cascade;
drop table project_file_measurement cascade;
drop table project_version_measurement cascade;
drop table metric cascade;
drop table metric_type;
drop table plugin;
drop table privilege;
drop table privilege cascade;
drop table project_file cascade;
drop table project_file_join;
drop table project_version;
drop table sender;
drop table service_url cascade;
drop table stored_project cascade;
drop table tag;
drop table userlogins;
drop table users;
drop table privilege_value cascade;
drop table evaluation_mark;
drop sequence hibernate_sequence;
drop table hibernate_unique_key;

-- Display remaining tables, just in case
--
\d

