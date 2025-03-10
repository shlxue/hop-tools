CREATE TABLE users
(
    id         bigint,
    name       varchar(32),
    created_at datetime default now(),
    updated_at datetime
);
CREATE TABLE orders
(
    id         bigint,
    user_id    bigint,
    name       varchar(32),
    created_at datetime,
    updated_at datetime
);
CREATE TABLE order_items
(
    id         bigint,
    order_id   bigint,
    name       varchar(32),
    created_at datetime,
    updated_at datetime
);

CREATE INDEX IX_NAME_ON_USERS on users (name);
CREATE INDEX IX_NAME_ON_ORDERS on orders (name);
CREATE INDEX IX_USER_ON_ORDERS on orders (user_id);
CREATE INDEX IX_NAME_ON_ORDER_ITEMS on order_items (order_id, name);
