# Frontend microservices "Passengeers"

## Устанавливаем версию ноды. Должна быть **v14.18.2** (прописана в .nvmrc)

`nvm use`

___

## Устанаваливаем пакеты строго по yarn.lock

`yarn ci`

или:

`yarn install --frozen-lockfile`

___

## Запуск приложения

`yarn start` запуск микрофронта (для подключения как Remote)

`yarn start:dev-autopark` запуск микрофронта как HOST (Main) который смотрит на dev стенд платформы

`start-auth:dev-autopark` запуск микрофронта c Basic авторизацией

`start-auth-mocked-login:dev-autopark` запуск микрофронта c Замоканной авторизацией

`start-auth-mocked-api:dev-autopark` запуск микрофронта c Basic авторизацией и моками по остальным Api

`start-auth-mocked-login-api:dev-autopark` запуск микрофронта c Замоканной авторизацией и остальным Api
