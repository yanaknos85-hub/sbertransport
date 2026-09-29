update request_aggregation.urls
set url = '/manager/leads/file/',
    pattern = '/manager/leads/file/'
where url = '/manager/leads/{userId}/file/';