CREATE TABLE migrations.changelog_geo (
	id varchar(255) NOT NULL,
	author varchar(255) NOT NULL,
	filename varchar(255) NOT NULL,
	dateexecuted timestamp NOT NULL,
	orderexecuted int4 NOT NULL,
	exectype varchar(10) NOT NULL,
	md5sum varchar(35) NULL,
	description varchar(255) NULL,
	"comments" varchar(255) NULL,
	tag varchar(255) NULL,
	liquibase varchar(20) NULL,
	contexts varchar(255) NULL,
	labels varchar(255) NULL,
	deployment_id varchar(10) NULL
);

CREATE TABLE migrations.changelog_geo_lock (
	id int4 NOT NULL,
	"locked" bool NOT NULL,
	lockgranted timestamp NULL,
	lockedby varchar(255) NULL,
	CONSTRAINT changelog_geo_lock_pkey PRIMARY KEY (id)
);

INSERT INTO migrations.changelog_geo (id,author,filename,dateexecuted,orderexecuted,exectype,md5sum,description,"comments",tag,liquibase,contexts,labels,deployment_id)
(select id,author,filename,dateexecuted,orderexecuted,exectype,md5sum,description,"comments",tag,liquibase,contexts,labels,deployment_id from migrations.databasechangelog)
on conflict do nothing;