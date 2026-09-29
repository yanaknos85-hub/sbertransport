alter table authorization_sbid.sessions drop column if exists token;

alter table authorization_sbid.sessions drop column if exists url;

alter table authorization_sbid.sessions drop column if exists nounce;

alter table authorization_sbid.sessions add column  creation_time timestamp not null;

alter table authorization_sbid.sessions add column  active boolean;

alter table authorization_sbid.sessions add column  refresh_token varchar(100);

alter table authorization_sbid.sessions add column  nonce varchar(100) not null;

alter table authorization_sbid.sessions add column  access_token varchar(100);

alter table authorization_sbid.sessions add column  id_token varchar(32000);

alter table authorization_sbid.sessions add column  expires_in integer;

alter table authorization_sbid.sessions add column  code varchar(100);

alter table authorization_sbid.sessions add sub varchar(300);
-- Column comments

comment on column authorization_sbid.sessions.creation_time is 'Время создания сессии';
comment on column authorization_sbid.sessions.active is 'Активна ли сессия';
comment on column authorization_sbid.sessions.refresh_token is 'Токен обновления';
comment on column authorization_sbid.sessions.access_token is 'Токен доступа';
comment on column authorization_sbid.sessions.id_token is 'Токен доступа';
comment on column authorization_sbid.sessions.expires_in is 'Время жизни токена';
comment on column authorization_sbid.sessions.code is 'Код авторизации';
comment on column authorization_sbid.sessions.nonce is 'Строковое значение, используемое для связи сеанса клиента с id_token и для предотвращения атак с повторным воспроизведением (replay-атак)';





