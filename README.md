### Запуск приложения

#### Запуск сервисов kafka

```shell
kubectl create namespace abdalovalex-l8-kafka && helm install kafka-service helm/kafka --namespace abdalovalex-l8-kafka
```

#### Запуск сервиса "Заказ"

```shell
kubectl create namespace abdalovalex-l8-order-service && helm install order-service helm/order-service --namespace abdalovalex-l8-order-service
```

#### Запуск сервиса "Уведомлений"

```shell
kubectl create namespace abdalovalex-l8-notification-service && helm install notification-service helm/notification-service --namespace abdalovalex-l8-notification-service
```

#### Запуск сервиса "Биллинг"

```shell
kubectl create namespace abdalovalex-l8-billing-service && helm install billing-service helm/billing-service --namespace abdalovalex-l8-billing-service
```

1. До установки запускаются следующие ресурсы:
    1. Запуск БД
    2. Установка ConfigMap и Secret
2. Запуск Deployment

Для порядка запуска используются initContainer и helm hook.  
ConfigMap и Secret забирает приложение(инструмент SpringCloudKubernetes), для этого создан ServiceAccount
с ограниченными ролями для приложения.

### Запуск тестов

```shell
newman run ./tests/tests.json
```

```
newman

otus_l8

→ Добавить счет
  POST http://arch.homework/billing-service/account/create [201 Created, 104B, 309ms]
  ✓  Добавить счет

→ Добавить уже существующий счет
  POST http://arch.homework/billing-service/account/create [400 Bad Request, 200B, 16ms]
  ✓  Добавить уже существующий счет

→ Положить деньги на счет
  PATCH http://arch.homework/billing-service/account/put [200 OK, 99B, 508ms]
  ✓  Положить деньги на счет

→ Положить деньги на счет (идемпотентность)
  PATCH http://arch.homework/billing-service/account/put [200 OK, 99B, 9ms]
  ✓  Положить деньги на счет (идемпотентность)

→ Получить баланс
  GET http://arch.homework/billing-service/account/78025498 [200 OK, 298B, 59ms]
  ✓  Получить баланс

→ Создать заказ 1
  POST http://arch.homework/order-service/order/create [201 Created, 146B, 1315ms]
  ✓  Создать заказ 1

→ Создать заказ 1 (идемпотентность)
  POST http://arch.homework/order-service/order/create [201 Created, 146B, 58ms]
  ✓  Создать заказ 1 (идемпотентность)

→ Получить баланс
  GET http://arch.homework/billing-service/account/78025498 [200 OK, 293B, 11ms]
  ✓  Получить баланс

→ Получить письмо по заказу 1
  GET http://arch.homework/notification-service/notification/1 [200 OK, 178B, 96ms]
  ✓  Получить письмо по заказу 1

→ Создать заказ 2
  POST http://arch.homework/order-service/order/create [201 Created, 146B, 12ms]
  ✓  Создать заказ 2

→ Получить баланс
  GET http://arch.homework/billing-service/account/78025498 [200 OK, 293B, 9ms]
  ✓  Получить баланс

→ Получить письмо по заказу 2
  GET http://arch.homework/notification-service/notification/2 [200 OK, 221B, 7ms]
  ✓  Получить письмо по заказу 2

┌─────────────────────────┬────────────────────┬────────────────────┐
│                         │           executed │             failed │
├─────────────────────────┼────────────────────┼────────────────────┤
│              iterations │                  1 │                  0 │
├─────────────────────────┼────────────────────┼────────────────────┤
│                requests │                 12 │                  0 │
├─────────────────────────┼────────────────────┼────────────────────┤
│            test-scripts │                 24 │                  0 │
├─────────────────────────┼────────────────────┼────────────────────┤
│      prerequest-scripts │                 21 │                  0 │
├─────────────────────────┼────────────────────┼────────────────────┤
│              assertions │                 12 │                  0 │
├─────────────────────────┴────────────────────┴────────────────────┤
│ total run duration: 6.6s                                          │
├───────────────────────────────────────────────────────────────────┤
│ total data received: 637B (approx)                                │
├───────────────────────────────────────────────────────────────────┤
│ average response time: 200ms [min: 7ms, max: 1315ms, s.d.: 366ms] │
└───────────────────────────────────────────────────────────────────┘
```

### Удаление

#### Kafka

```shell
helm uninstall kafka -n abdalovalex-l8-kafka  
kubectl delete namespace abdalovalex-l8-kafka 
````

#### Удалить сервис "Заказ"

```shell
helm uninstall order-service -n abdalovalex-l8-order-service  
kubectl delete namespace abdalovalex-l8-order-service
kubectl delete clusterrolebinding order-service
kubectl delete clusterrole order-service 
````

#### Удалить сервис "Уведомлений"

```shell
helm uninstall notification-service -n abdalovalex-l8-notification-service
kubectl delete namespace abdalovalex-l8-notification-service
kubectl delete clusterrolebinding notification-service
kubectl delete clusterrole notification-service
```

#### Удалить сервис "Биллинг"

```shell
helm uninstall billing-service -n abdalovalex-l8-billing-service
kubectl delete namespace abdalovalex-l8-billing-service
kubectl delete clusterrolebinding billing-service
kubectl delete clusterrole billing-service
```

### Архитектурное решение

Решение основано на kafka streams.  
Топология стримов  
![streams.png](scheme/streams.png)

1. Создается заказ
2. Заказ попадает в топик "order"
3. Сервис биллинга, считывает топик "order" и перенаправляет события в топик bankTransaction
4. Далее обрабатывается топик bankTransaction, в котором происходит агрегация банковских
   операций, для вычисления баланса. И сохраняется в хранилище bankBalanceStore(kafka)
5. Затем из потока вытаскиваются, только банковские операции, которые относятся к заказам.
   И перенаправляются в топик approvedOrderTransaction для успешных операций или в rejectedOrderTransaction
   для неуспешных
6. Сервис уведомлений считывает топик approvedOrderTransaction для отправки успешных уведомлений
   и топик rejectedOrderTransaction для неуспешных уведомлений

Для получения баланса, делается запрос в хранилище bankBalanceStore(kafka).

#### Идемпотентность

1. Пополнения денег. Для каждой операции генерируется хеш. Если такой хеш уже присутствует,
   то операция пополнения не выполняется. Так же, если приходит несколько запросов одновременно с одинаковым хешом,
   то сохраниться только один, так как на поле хеш в бд стоит уникальный индекс.
2. Создание заказа. Для каждой операции генерируется хеш. Если такой хеш уже присутствует,
   то операция заказа не выполняется. Так же, если приходит несколько запросов одновременно с одинаковым хешом,
   то сохраниться только один, так как на поле хеш в бд стоит уникальный индекс.

#### AsyncAPI документация

**Биллинг**: http://arch.homework/billing-service/springwolf/asyncapi-ui.html  
**Уведомления** http://arch.homework/notification-service/springwolf/asyncapi-ui.html
