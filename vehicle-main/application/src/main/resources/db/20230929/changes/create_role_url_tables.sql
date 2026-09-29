CREATE TABLE IF NOT EXISTS "vehicle"."urls" (
                                             "id" UUID PRIMARY KEY,
                                             "url" varchar(255) NOT NULL,
    "pattern" varchar(255) NOT NULL,
    "method" varchar(255) NOT NULL,
    constraint vehicle_urls_method_url_uk UNIQUE("url", "method"),
    constraint vehicle_urls_method_url_pattern_uk UNIQUE("url", "method", "pattern")
    );

CREATE TABLE IF NOT EXISTS "vehicle"."roles" (
    "role" VARCHAR(255) NOT NULL,
    "url_id" UUID NOT NULL,
    primary key ("role", "url_id")
    );