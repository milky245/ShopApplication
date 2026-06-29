/*
 * Repair SQL for databases initialized by the first version of
 * init_all_spirit_shop.sql.
 *
 * Fixes coupon table columns to match the current JPA entities and removes
 * seeded reviews so the initial client user can open the review form.
 */

-- coupon_types: add columns required by CouponType entity.
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE coupon_types ADD (minimum_order_amount NUMBER(8) DEFAULT 0 NOT NULL)';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1430 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE coupon_types ADD (validity_days NUMBER(4) DEFAULT 30 NOT NULL)';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1430 THEN RAISE; END IF;
END;
/

UPDATE coupon_types
SET minimum_order_amount =
  CASE discount_rate
    WHEN 5 THEN 500
    WHEN 10 THEN 1000
    WHEN 15 THEN 2000
    ELSE 0
  END,
  validity_days = 30;

-- Recreate user_coupons with the column names used by UserCoupon entity.
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE user_coupons CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_user_coupons';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2289 THEN RAISE; END IF;
END;
/

CREATE TABLE user_coupons (
  id NUMBER(10) CONSTRAINT pk_user_coupons PRIMARY KEY,
  user_id NUMBER(6) NOT NULL,
  coupon_type_id NUMBER(6) NOT NULL,
  acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_user_coupons_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_coupons_type FOREIGN KEY (coupon_type_id) REFERENCES coupon_types(id),
  CONSTRAINT ck_user_coupons_expiry CHECK (expires_at > acquired_at)
);

CREATE SEQUENCE seq_user_coupons START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE INDEX idx_user_coupons_user_expiry ON user_coupons(user_id, expires_at);

INSERT INTO user_coupons (id, user_id, coupon_type_id, acquired_at, expires_at)
VALUES (
  seq_user_coupons.NEXTVAL,
  (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'),
  (SELECT id FROM coupon_types WHERE discount_rate = 10),
  DEFAULT,
  SYSTIMESTAMP + INTERVAL '30' DAY
);

-- Recreate coupon_gacha_histories with the column names used by CouponGachaHistory entity.
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE coupon_gacha_histories CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_coupon_gacha_histories';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2289 THEN RAISE; END IF;
END;
/

CREATE TABLE coupon_gacha_histories (
  id NUMBER(10) CONSTRAINT pk_coupon_gacha_histories PRIMARY KEY,
  user_id NUMBER(6) NOT NULL,
  business_date DATE NOT NULL,
  result_coupon_type_id NUMBER(6),
  drawn_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_coupon_gacha_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_coupon_gacha_type FOREIGN KEY (result_coupon_type_id) REFERENCES coupon_types(id),
  CONSTRAINT uq_coupon_gacha_daily UNIQUE (user_id, business_date)
);

CREATE SEQUENCE seq_coupon_gacha_histories START WITH 1 INCREMENT BY 1 NOCACHE;

-- Remove the two seeded reviews from the old initialization script.
DELETE FROM reviews
WHERE user_id = (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp')
  AND order_item_id IN (1, 3);

COMMIT;
