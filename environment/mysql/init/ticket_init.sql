-- Deterministic local schema for the event ticket booking product.
-- Docker Compose creates and selects the `vetautet` database through MYSQL_DATABASE.

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(190) NOT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(120) NOT NULL,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `provider` VARCHAR(32) NULL,
    `provider_id` VARCHAR(190) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(32) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_roles_name` (`name`),
    CONSTRAINT `ck_roles_name` CHECK (`name` IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `events` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(190) NOT NULL,
    `description` TEXT NULL,
    `venue` VARCHAR(190) NOT NULL,
    `start_at` TIMESTAMP NOT NULL,
    `end_at` TIMESTAMP NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_events_active` (`active`),
    KEY `idx_events_start_at` (`start_at`),
    CONSTRAINT `ck_events_time_range` CHECK (`end_at` > `start_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ticket_types` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `event_id` BIGINT NOT NULL,
    `name` VARCHAR(120) NOT NULL,
    `description` TEXT NULL,
    `price` DECIMAL(12,2) NOT NULL,
    `stock_initial` INT NOT NULL,
    `stock_available` INT NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_ticket_types_event_id` (`event_id`),
    KEY `idx_ticket_types_active` (`active`),
    CONSTRAINT `fk_ticket_types_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`),
    CONSTRAINT `ck_ticket_types_price_positive` CHECK (`price` > 0),
    CONSTRAINT `ck_ticket_types_stock_initial_non_negative` CHECK (`stock_initial` >= 0),
    CONSTRAINT `ck_ticket_types_stock_available_non_negative` CHECK (`stock_available` >= 0),
    CONSTRAINT `ck_ticket_types_stock_available_lte_initial` CHECK (`stock_available` <= `stock_initial`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_number` VARCHAR(64) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `total_amount` DECIMAL(12,2) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orders_order_number` (`order_number`),
    KEY `idx_orders_user_id` (`user_id`),
    KEY `idx_orders_status` (`status`),
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `ck_orders_status` CHECK (`status` IN ('PENDING', 'PAID', 'CANCELLED', 'PAYMENT_FAILED', 'EXPIRED')),
    CONSTRAINT `ck_orders_total_amount_non_negative` CHECK (`total_amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `ticket_type_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(12,2) NOT NULL,
    `total_price` DECIMAL(12,2) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_items_order_id` (`order_id`),
    KEY `idx_order_items_ticket_type_id` (`ticket_type_id`),
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
    CONSTRAINT `fk_order_items_ticket_type` FOREIGN KEY (`ticket_type_id`) REFERENCES `ticket_types` (`id`),
    CONSTRAINT `ck_order_items_quantity_positive` CHECK (`quantity` > 0),
    CONSTRAINT `ck_order_items_unit_price_positive` CHECK (`unit_price` > 0),
    CONSTRAINT `ck_order_items_total_price_positive` CHECK (`total_price` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `payment_id` VARCHAR(64) NOT NULL,
    `order_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `amount` DECIMAL(12,2) NOT NULL,
    `method` VARCHAR(32) NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `gateway_transaction_id` VARCHAR(120) NULL,
    `payment_url` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payments_payment_id` (`payment_id`),
    KEY `idx_payments_order_id` (`order_id`),
    KEY `idx_payments_user_id` (`user_id`),
    CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
    CONSTRAINT `fk_payments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `ck_payments_amount_non_negative` CHECK (`amount` >= 0),
    CONSTRAINT `ck_payments_status` CHECK (`status` IN ('INIT', 'PENDING', 'SUCCESS', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `roles` (`id`, `name`) VALUES
    (1, 'USER'),
    (2, 'ADMIN');

INSERT INTO `users` (`id`, `email`, `password_hash`, `full_name`, `enabled`, `provider`) VALUES
    (1, 'admin@example.com', '$2a$10$7qBPDgt2/lhV67XftVB3FOJ5dKAq235bTB76vT0KkcTzxVaz..wTO', 'Admin User', TRUE, 'LOCAL'),
    (2, 'user@example.com', '$2a$10$cBx3SZADoTFDM7SpCw/Oue1VleVgdeg5SZd844Oah1uqZZL.0lM.K', 'Normal User', TRUE, 'LOCAL');

INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
    (1, 1),
    (1, 2),
    (2, 1);

INSERT INTO `events` (`id`, `title`, `description`, `venue`, `start_at`, `end_at`, `active`) VALUES
    (1, 'Spring Music Festival', 'Sample active event for local development', 'Ho Chi Minh City', '2026-06-20 18:00:00', '2026-06-20 23:00:00', TRUE),
    (2, 'Archived Tech Meetup', 'Sample inactive event for filtering checks', 'Da Nang', '2026-04-12 09:00:00', '2026-04-12 17:00:00', FALSE);

INSERT INTO `ticket_types` (`id`, `event_id`, `name`, `description`, `price`, `stock_initial`, `stock_available`, `active`) VALUES
    (1, 1, 'General Admission', 'Standard access ticket', 250000.00, 1000, 1000, TRUE),
    (2, 1, 'VIP', 'VIP access ticket', 750000.00, 100, 100, TRUE),
    (3, 2, 'Archived General', 'Inactive event sample ticket', 150000.00, 50, 50, FALSE);
