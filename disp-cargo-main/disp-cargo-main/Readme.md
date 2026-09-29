# Disp frontend microservices "Cargo"

## Устанавливаем версию ноды (прописана в .nvmrc)

`nvm use`

___

## Устанаваливаем пакеты строго по yarn.lock

`yarn ci`

или:

`yarn install --frozen-lockfile`

___

## Запуск приложения

`yarn start` запуск микрофронта (для подключения как remote)

`yarn start:dev-cargo` запуск микрофронта как HOST (Main) который смотрит на dev стенд платформы

`start-auth:dev-cargo` запуск микрофронта c Basic авторизацией

`start-auth-mocked-login:dev-cargo` запуск микрофронта c Замоканной авторизацией

`start-auth-mocked-api:dev-cargo` запуск микрофронта c Basic авторизацией и моками по остальным Api

`start-auth-mocked-login-api:dev-cargo` запуск микрофронта c Замоканной авторизацией и остальным Api
