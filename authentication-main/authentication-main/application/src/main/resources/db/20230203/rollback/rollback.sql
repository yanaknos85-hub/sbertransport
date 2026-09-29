alter table authentication.role alter column default_for drop not null;
alter table authentication.role alter column default_for drop default;
update authentication.role set default_for = null where default_for = '[]';