# Frontend microservices "Main"

---

## Устанавливаем версию ноды. Должна быть **v14.18.2** (прописана в .nvmrc)

`nvm use`

или

`nvm use lts/fermium`

---

## Устанаваливаем пакеты строго по yarn.lock

`yarn ci`

или:

`yarn install --frozen-lockfile`

---

## Запуск приложения (пример)

`yarn start:dev-autopark` - Судир
`yarn start-auth:dev-autopark` - Лог/пасс авторизация
