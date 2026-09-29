alter table request_audit.request_for_public
    drop constraint request_for_public_rev_fkey;
alter table request_audit.request_for_public_revinfo
    drop constraint request_for_public_revinfo_pkey,
    drop column rev,
    add column rev bigserial,
    add constraint request_for_public_revinfo_pkey primary key (rev);
alter table request_audit.request_for_public
    add constraint request_for_public_rev_fkey foreign key (rev) references request_audit.request_for_public_revinfo (rev);