update authentication.role set default_for = '[]' where default_for is null;
alter table authentication.role alter column default_for set not null;
alter table authentication.role alter column default_for set default '[]';