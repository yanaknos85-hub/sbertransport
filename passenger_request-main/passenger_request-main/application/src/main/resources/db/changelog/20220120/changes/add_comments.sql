alter table request.request_for_import rename column dto_json to data;
comment on column request.request_for_import.count_all is 'Количество строк в загружаемом файле';
comment on column request.request_for_import.data is 'Загруженные данные';
comment on column request.request_for_import.description is 'Описание при статусе ERROR';
comment on column request.request_for_import.status is 'Статус загрузки SUCESS ERROR RUNNING';
comment on column request.request_for_import.count is 'Количество уже загруженных строк';
comment on column request.request_for_import.date is 'Дата загрузки';