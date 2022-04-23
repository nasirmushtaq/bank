CREATE database test;
use test;


CREATE TABLE `account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` varchar(40) NOT NULL,
  `name` varchar(70) NOT NULL,
  `nick_name` varchar(20) ,
  `status` varchar(20) NOT NULL,
  `routing_no` varchar(10) NOT NULL,
  `account_no` varchar(20) NOT NULL,
  `email` varchar(70) NOT NULL,
  `phone_no` varchar(13) NOT NULL,
  `deleted` tinyint(1) default 0,
  `active` tinyint(1) GENERATED ALWAYS AS (if((`deleted` = 0),1,NULL)) VIRTUAL,
   PRIMARY KEY (`id`),
   KEY `user_id_idx` (`user_id`),
   CONSTRAINT  UNIQUE (email, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



CREATE TABLE `wallet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` varchar(40) NOT NULL,
  `currency` varchar(5) NOT NULL,
  `amount` Decimal(60, 2) NOT NULL DEFAULT 0,
   PRIMARY KEY (`id`),
   KEY `user_id_idx` (`user_id`),
   CONSTRAINT  UNIQUE (`user_id`, `currency`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `transaction_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `source_user_id` varchar(40) NOT NULL,
  `target_user_id` varchar(40) NOT NULL,
  `transaction_id` varchar(40) NOT NULL,
  `status` varchar(10) NOT NULL,
  `source_currency` varchar(5) NOT NULL,
  `target_currency` varchar(5) NOT NULL,
  `source_amount` Decimal(60, 2) NOT NULL DEFAULT 0,
  `target_amount` Decimal(60, 2) NOT NULL DEFAULT 0,
  `rate` Decimal(60, 2) NOT NULL DEFAULT 0,
   PRIMARY KEY (`id`),
   KEY `user_id_idx` (`source_user_id`),
   CONSTRAINT  UNIQUE (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;