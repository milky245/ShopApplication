/*
 * shared_shop full initialization SQL
 *
 * dev_for_test の追加機能用SQLを統合し、精霊ショップの商品データで初期化します。
 * 実行すると既存テーブル/Sequenceを削除して再作成します。
 */

-- ============================================================
-- Drop tables
-- ============================================================
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE reviews CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE favorite CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE view_histories CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE login_histories CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE coupon_gacha_histories CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE user_coupons CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE delivery_addresses CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE order_items CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE orders CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE coupon_types CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE items CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE categories CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE users CASCADE CONSTRAINTS PURGE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

-- ============================================================
-- Drop sequences
-- ============================================================
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_reviews'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_favorite'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_view_histories'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_login_histories'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_coupon_gacha_histories'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_user_coupons'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_coupon_types'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_delivery_addresses'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_order_items'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_orders'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_items'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_categories'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE seq_users'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;
/

PURGE RECYCLEBIN;

-- ============================================================
-- Base tables
-- ============================================================
CREATE TABLE users (
  id NUMBER(6) CONSTRAINT pk_users PRIMARY KEY,
  email VARCHAR2(256 CHAR) NOT NULL CONSTRAINT uk_users_email UNIQUE,
  password VARCHAR2(16 CHAR) NOT NULL,
  name VARCHAR2(30 CHAR) NOT NULL,
  postal_code VARCHAR2(7 CHAR) NOT NULL,
  address VARCHAR2(150 CHAR) NOT NULL,
  phone_number VARCHAR2(11 CHAR) NOT NULL,
  authority NUMBER(1) NOT NULL,
  delete_flag NUMBER(1) DEFAULT 0 NOT NULL,
  insert_date DATE DEFAULT SYSDATE NOT NULL,
  login_failure_count NUMBER(10) DEFAULT 0 NOT NULL,
  lock_release_time TIMESTAMP,
  point NUMBER(10) DEFAULT 0 NOT NULL,
  CONSTRAINT ck_users_authority CHECK (authority IN (0, 1, 2)),
  CONSTRAINT ck_users_delete_flag CHECK (delete_flag IN (0, 1)),
  CONSTRAINT ck_users_point CHECK (point >= 0)
);

CREATE TABLE categories (
  id NUMBER(2) CONSTRAINT pk_categories PRIMARY KEY,
  name VARCHAR2(30 CHAR) NOT NULL,
  description VARCHAR2(100 CHAR),
  delete_flag NUMBER(1) DEFAULT 0 NOT NULL,
  insert_date DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT ck_categories_delete_flag CHECK (delete_flag IN (0, 1))
);

CREATE TABLE items (
  id NUMBER(6) CONSTRAINT pk_items PRIMARY KEY,
  name VARCHAR2(100 CHAR) NOT NULL,
  price NUMBER(7) NOT NULL,
  description VARCHAR2(1000 CHAR),
  stock NUMBER(4) NOT NULL,
  image VARCHAR2(150 CHAR),
  category_id NUMBER(2) NOT NULL,
  delete_flag NUMBER(1) DEFAULT 0 NOT NULL,
  insert_date DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT ck_items_price CHECK (price >= 0),
  CONSTRAINT ck_items_stock CHECK (stock >= 0),
  CONSTRAINT ck_items_delete_flag CHECK (delete_flag IN (0, 1))
);

CREATE TABLE coupon_types (
  id NUMBER(6) CONSTRAINT pk_coupon_types PRIMARY KEY,
  name VARCHAR2(50 CHAR) NOT NULL,
  discount_rate NUMBER(3) NOT NULL,
  minimum_order_amount NUMBER(8) NOT NULL,
  validity_days NUMBER(4) DEFAULT 30 NOT NULL,
  active_flag NUMBER(1) DEFAULT 1 NOT NULL,
  insert_date DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT ck_coupon_types_rate CHECK (discount_rate BETWEEN 1 AND 100),
  CONSTRAINT ck_coupon_types_min_amount CHECK (minimum_order_amount >= 0),
  CONSTRAINT ck_coupon_types_validity CHECK (validity_days > 0),
  CONSTRAINT ck_coupon_types_active CHECK (active_flag IN (0, 1))
);

CREATE TABLE orders (
  id NUMBER(6) CONSTRAINT pk_orders PRIMARY KEY,
  postal_code VARCHAR2(7 CHAR) NOT NULL,
  address VARCHAR2(150 CHAR) NOT NULL,
  name VARCHAR2(30 CHAR) NOT NULL,
  phone_number VARCHAR2(11 CHAR) NOT NULL,
  pay_method NUMBER(1) NOT NULL,
  user_id NUMBER(6) NOT NULL,
  insert_date DATE DEFAULT SYSDATE NOT NULL,
  delivery_date DATE,
  coupon_type_id NUMBER(6),
  coupon_name VARCHAR2(50 CHAR),
  coupon_discount_rate NUMBER(3),
  coupon_discount_amount NUMBER(7) DEFAULT 0 NOT NULL,
  use_point NUMBER(10) DEFAULT 0 NOT NULL,
  earned_point NUMBER(10) DEFAULT 0 NOT NULL,
  cancel_flag NUMBER(1) DEFAULT 0 NOT NULL,
  cancel_date DATE,
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_orders_coupon_type FOREIGN KEY (coupon_type_id) REFERENCES coupon_types(id),
  CONSTRAINT ck_orders_coupon_rate CHECK (coupon_discount_rate IS NULL OR coupon_discount_rate BETWEEN 1 AND 100),
  CONSTRAINT ck_orders_coupon_amount CHECK (coupon_discount_amount >= 0),
  CONSTRAINT ck_orders_use_point CHECK (use_point >= 0),
  CONSTRAINT ck_orders_earned_point CHECK (earned_point >= 0),
  CONSTRAINT ck_orders_cancel_flag CHECK (cancel_flag IN (0, 1))
);

CREATE TABLE order_items (
  id NUMBER(6) CONSTRAINT pk_order_items PRIMARY KEY,
  quantity NUMBER(4) NOT NULL,
  order_id NUMBER(6) NOT NULL,
  item_id NUMBER(6) NOT NULL,
  price NUMBER(7) NOT NULL,
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_items_item FOREIGN KEY (item_id) REFERENCES items(id),
  CONSTRAINT ck_order_items_quantity CHECK (quantity > 0),
  CONSTRAINT ck_order_items_price CHECK (price >= 0)
);

-- ============================================================
-- Feature tables
-- ============================================================
CREATE TABLE delivery_addresses (
  id NUMBER(6) CONSTRAINT pk_delivery_addresses PRIMARY KEY,
  user_id NUMBER(6) NOT NULL,
  address_no NUMBER(1) NOT NULL,
  name VARCHAR2(50 CHAR) NOT NULL,
  postal_code VARCHAR2(10 CHAR) NOT NULL,
  address VARCHAR2(255 CHAR) NOT NULL,
  phone_number VARCHAR2(20 CHAR) NOT NULL,
  CONSTRAINT fk_delivery_addresses_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT uk_delivery_addresses_user_no UNIQUE (user_id, address_no),
  CONSTRAINT ck_delivery_address_no CHECK (address_no BETWEEN 1 AND 3)
);

CREATE TABLE login_histories (
  id NUMBER(10) CONSTRAINT pk_login_histories PRIMARY KEY,
  user_id NUMBER(6) NOT NULL,
  login_date_time TIMESTAMP NOT NULL,
  ip_address VARCHAR2(45 CHAR) NOT NULL,
  CONSTRAINT fk_login_histories_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE view_histories (
  id NUMBER(10) CONSTRAINT pk_view_histories PRIMARY KEY,
  user_id NUMBER(6) NOT NULL,
  item_id NUMBER(6) NOT NULL,
  view_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_view_histories_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_view_histories_item FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE TABLE favorite (
  id NUMBER(10) CONSTRAINT pk_favorite PRIMARY KEY,
  user_id NUMBER(6) NOT NULL,
  item_id NUMBER(6) NOT NULL,
  insert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_favorite_item FOREIGN KEY (item_id) REFERENCES items(id),
  CONSTRAINT uk_favorite_user_item UNIQUE (user_id, item_id)
);

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

CREATE TABLE reviews (
  id NUMBER(10) CONSTRAINT pk_reviews PRIMARY KEY,
  user_id NUMBER(6) NOT NULL,
  item_id NUMBER(6) NOT NULL,
  order_item_id NUMBER(6) NOT NULL,
  rating NUMBER(1) NOT NULL,
  review_comment VARCHAR2(2000 CHAR),
  insert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_reviews_item FOREIGN KEY (item_id) REFERENCES items(id),
  CONSTRAINT fk_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id),
  CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_item ON order_items(item_id);
CREATE INDEX idx_delivery_addr_user ON delivery_addresses(user_id);
CREATE INDEX idx_login_histories_user ON login_histories(user_id, login_date_time);
CREATE INDEX idx_view_histories_user ON view_histories(user_id, view_date);
CREATE INDEX idx_view_histories_item ON view_histories(item_id);
CREATE INDEX idx_user_coupons_user_expiry ON user_coupons(user_id, expires_at);
CREATE INDEX idx_reviews_item_date ON reviews(item_id, insert_date);
CREATE INDEX idx_reviews_user ON reviews(user_id);

-- ============================================================
-- Sequences
-- ============================================================
CREATE SEQUENCE seq_users START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_categories START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_items START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_coupon_types START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_orders START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_order_items START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_delivery_addresses START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_login_histories START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_view_histories START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_favorite START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_user_coupons START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_coupon_gacha_histories START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_reviews START WITH 1 INCREMENT BY 1 NOCACHE;

-- ============================================================
-- Initial data: categories
-- ============================================================
INSERT INTO categories (id, name, description, delete_flag, insert_date)
VALUES (seq_categories.NEXTVAL, '精霊', '契約可能な精霊を扱います。', DEFAULT, DEFAULT);

INSERT INTO categories (id, name, description, delete_flag, insert_date)
VALUES (seq_categories.NEXTVAL, '魔道具', '精霊との生活を支える魔道具を扱います。', DEFAULT, DEFAULT);

INSERT INTO categories (id, name, description, delete_flag, insert_date)
VALUES (seq_categories.NEXTVAL, '餌', '精霊たちの好物や栄養食を扱います。', DEFAULT, DEFAULT);

INSERT INTO categories (id, name, description, delete_flag, insert_date)
VALUES (seq_categories.NEXTVAL, '契約用品', '契約や召喚に必要な用品を扱います。', DEFAULT, DEFAULT);

INSERT INTO categories (id, name, description, delete_flag, insert_date)
VALUES (seq_categories.NEXTVAL, '住処', '精霊が快適に暮らすための住処を扱います。', DEFAULT, DEFAULT);

-- ============================================================
-- Initial data: items
-- 画像ファイル名は商品名 + ".png" に統一しています。
-- ============================================================
INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, 'アライグマの精霊', 8000, 'いたずら好きで好奇心旺盛な小型精霊。宙をふわりと漂いながら持ち主のそばで暮らします。探し物を見つけるのが得意で、懐くと小さな幸運を運んできてくれると言われています。', 8, 'アライグマの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, 'キツネの精霊', 12000, '紅葉色の尾を持つ賢い精霊。夜道では淡い灯火を生み出し、持ち主を安全な道へ導いてくれます。', 5, 'キツネの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, 'ウサギの精霊', 6500, 'ふわふわの体で宙を跳ね回る精霊。周囲の植物を元気にする力を持ち、初心者にも人気があります。', 10, 'ウサギの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, 'カワウソの精霊', 9500, '澄んだ水を好む水属性の精霊。小さな水球を作って遊び、乾燥した部屋に心地よい潤いを与えてくれます。', 6, 'カワウソの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, 'フクロウの精霊', 15000, '夜空を静かに漂う知恵の精霊。持ち主が勉強や仕事に集中できるよう穏やかな魔力で支えてくれます。', 3, 'フクロウの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, 'クラゲの精霊', 11000, '星明かりのような光を放ちながら浮遊する幻想的な精霊。見ているだけで心が落ち着く癒やし系として人気です。', 4, 'クラゲの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '【希少種】月光オオカミの精霊', 45000, '満月の夜にのみ姿を現す希少な精霊。銀色の毛並みは月光を浴びると輝きを増し、持ち主を危険から守ると言われています。', 1, '【希少種】月光オオカミの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '【希少種】星屑ネコの精霊', 38000, '夜空の星々から生まれたとされる希少種。歩いた後には小さな光の粒が舞い、見る者の心を癒やします。', 2, '【希少種】星屑ネコの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '【希少種】虹羽ペンギンの精霊', 52000, '七色に輝く羽根を持つ極めて珍しい精霊。幸運を引き寄せる力があり、王侯貴族にも人気があります。', 1, '【希少種】虹羽ペンギンの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '【希少種】雲海ラッコの精霊', 42000, '高空の雲海を漂う希少な精霊。穏やかな性格で、持ち主の疲れや不安を和らげる不思議な力を持ちます。', 2, '【希少種】雲海ラッコの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '【希少種】王冠アライグマの精霊', 60000, 'アライグマの精霊の突然変異種。額に王冠のような光輪を宿しており、精霊研究家の間では幻の存在として知られています。', 1, '【希少種】王冠アライグマの精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '【伝説級】始祖竜の精霊', 300000, '精霊界の最上位種。契約成功例は極めて少なく、その存在は神話にも記されている。穏やかな性格だが膨大な魔力を秘めており、契約者に絶大な恩恵をもたらす。※購入には王立精霊協会の審査が必要です。', 1, '【伝説級】始祖竜の精霊.png', (SELECT id FROM categories WHERE name = '精霊'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '月光ベリー', 1200, '夜にだけ実る果実。小型精霊に人気があり、魔力の回復を助けます。', 30, '月光ベリー.png', (SELECT id FROM categories WHERE name = '餌'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '星屑クッキー', 1800, '星の砂を練り込んだおやつ。精霊たちのご褒美として親しまれています。', 25, '星屑クッキー.png', (SELECT id FROM categories WHERE name = '餌'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '虹蜜ゼリー', 2200, '七色の花から採取した蜜を使用。甘い香りで多くの精霊を魅了します。', 20, '虹蜜ゼリー.png', (SELECT id FROM categories WHERE name = '餌'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '森の木の実セット', 1500, '自然属性の精霊向けに調合された栄養満点の木の実セットです。', 40, '森の木の実セット.png', (SELECT id FROM categories WHERE name = '餌'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '精霊ミルク', 2800, '幼い精霊の成長を支える特製ミルク。初心者の契約者にもおすすめです。', 15, '精霊ミルク.png', (SELECT id FROM categories WHERE name = '餌'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '王家御用達プレミアムフード', 12000, '希少な魔力植物のみを使用した最高級精霊食。希少種や上位精霊に人気があります。', 5, '王家御用達プレミアムフード.png', (SELECT id FROM categories WHERE name = '餌'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '竜鱗ビスケット', 18000, '伝説級精霊向けに開発された特別食。濃縮された魔力を含みます。', 2, '竜鱗ビスケット.png', (SELECT id FROM categories WHERE name = '餌'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '契約の首輪', 5000, '精霊との絆を安定させる初心者向け魔道具。初めて精霊を迎える方におすすめです。', 15, '契約の首輪.png', (SELECT id FROM categories WHERE name = '魔道具'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '浮遊追跡タグ', 3500, '精霊の現在位置を確認できる便利な魔道具。いたずら好きな精霊の見守りに最適です。', 20, '浮遊追跡タグ.png', (SELECT id FROM categories WHERE name = '魔道具'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '精霊ブラシ', 2800, '毛並みを持つ精霊専用のブラシ。使用すると機嫌が良くなると言われています。', 30, '精霊ブラシ.png', (SELECT id FROM categories WHERE name = '魔道具'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '魔力測定器', 12000, '精霊の魔力量や健康状態を測定できる定番アイテムです。', 8, '魔力測定器.png', (SELECT id FROM categories WHERE name = '魔道具'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '自動給餌オーブ', 15000, '登録した時間に自動で餌を与える便利な魔道具です。', 6, '自動給餌オーブ.png', (SELECT id FROM categories WHERE name = '魔道具'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '契約者証クリスタル', 12000, '契約した精霊の情報を記録するクリスタル。複数の精霊を管理する際に便利です。', 8, '契約者証クリスタル.png', (SELECT id FROM categories WHERE name = '契約用品'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '月光の巣', 8000, '月の魔力を蓄えた柔らかな寝床。小型精霊に人気の定番住処です。', 10, '月光の巣.png', (SELECT id FROM categories WHERE name = '住処'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '浮遊ツリーハウス', 18000, '空中に浮かぶ樹木型の住居。活発な精霊が遊びながら暮らせます。', 5, '浮遊ツリーハウス.png', (SELECT id FROM categories WHERE name = '住処'), DEFAULT, DEFAULT);

INSERT INTO items (id, name, price, description, stock, image, category_id, delete_flag, insert_date)
VALUES (seq_items.NEXTVAL, '星見のドーム', 35000, '夜空の魔力を取り込む高級住居。希少種や上位精霊向けの人気商品です。', 2, '星見のドーム.png', (SELECT id FROM categories WHERE name = '住処'), DEFAULT, DEFAULT);

-- ============================================================
-- Initial data: coupon types
-- ============================================================
INSERT INTO coupon_types (id, name, discount_rate, minimum_order_amount, validity_days, active_flag, insert_date)
VALUES (seq_coupon_types.NEXTVAL, '5%割引クーポン', 5, 500, 30, DEFAULT, DEFAULT);

INSERT INTO coupon_types (id, name, discount_rate, minimum_order_amount, validity_days, active_flag, insert_date)
VALUES (seq_coupon_types.NEXTVAL, '10%割引クーポン', 10, 1000, 30, DEFAULT, DEFAULT);

INSERT INTO coupon_types (id, name, discount_rate, minimum_order_amount, validity_days, active_flag, insert_date)
VALUES (seq_coupon_types.NEXTVAL, '15%割引クーポン', 15, 2000, 30, DEFAULT, DEFAULT);

-- ============================================================
-- Initial data: users
-- ============================================================
INSERT INTO users (id, email, password, name, postal_code, address, phone_number, authority, delete_flag, insert_date, login_failure_count, lock_release_time, point)
VALUES (seq_users.NEXTVAL, 'tanaka_taro@test.co.jp', 'Testtest0', 'システム管理太郎', '1111111', '東京都台東区1-2-3 ABCビル10階', '0123456789', 0, DEFAULT, DEFAULT, DEFAULT, NULL, DEFAULT);

INSERT INTO users (id, email, password, name, postal_code, address, phone_number, authority, delete_flag, insert_date, login_failure_count, lock_release_time, point)
VALUES (seq_users.NEXTVAL, 'unyo_jiro@test.co.jp', 'Testtest1', '運用管理二郎', '1111111', '東京都台東区1-2-3 ABCビル10階', '0123456789', 1, DEFAULT, DEFAULT, DEFAULT, NULL, DEFAULT);

INSERT INTO users (id, email, password, name, postal_code, address, phone_number, authority, delete_flag, insert_date, login_failure_count, lock_release_time, point)
VALUES (seq_users.NEXTVAL, 'ippan_saburo@test.co.jp', 'Testtest2', '一般三郎', '1111111', '東京都台東区4-5-6 ABCマンション5階', '0123456789', 2, DEFAULT, DEFAULT, DEFAULT, NULL, 1000);

-- ============================================================
-- Initial data: delivery addresses
-- ============================================================
INSERT INTO delivery_addresses (id, user_id, address_no, name, postal_code, address, phone_number)
VALUES (seq_delivery_addresses.NEXTVAL, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), 1, '一般三郎', '1111111', '東京都台東区4-5-6 ABCマンション5階', '0123456789');

-- ============================================================
-- Initial data: orders
-- ============================================================
INSERT INTO orders (id, postal_code, address, name, phone_number, pay_method, user_id, insert_date, delivery_date, coupon_type_id, coupon_name, coupon_discount_rate, coupon_discount_amount, use_point, earned_point, cancel_flag, cancel_date)
VALUES (seq_orders.NEXTVAL, '1111111', '東京都台東区4-5-6 ABCマンション5階', '一般三郎', '0123456789', 2, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), DEFAULT, TRUNC(SYSDATE) + 3, NULL, NULL, NULL, DEFAULT, 0, 92, DEFAULT, NULL);

INSERT INTO orders (id, postal_code, address, name, phone_number, pay_method, user_id, insert_date, delivery_date, coupon_type_id, coupon_name, coupon_discount_rate, coupon_discount_amount, use_point, earned_point, cancel_flag, cancel_date)
VALUES (seq_orders.NEXTVAL, '1111111', '東京都台東区4-5-6 ABCマンション5階', '一般三郎', '0123456789', 2, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), DEFAULT, TRUNC(SYSDATE) + 5, NULL, NULL, NULL, DEFAULT, 0, 138, DEFAULT, NULL);

INSERT INTO orders (id, postal_code, address, name, phone_number, pay_method, user_id, insert_date, delivery_date, coupon_type_id, coupon_name, coupon_discount_rate, coupon_discount_amount, use_point, earned_point, cancel_flag, cancel_date)
VALUES (seq_orders.NEXTVAL, '1111111', '東京都台東区4-5-6 ABCマンション5階', '一般三郎', '0123456789', 2, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), DEFAULT, TRUNC(SYSDATE) + 7, NULL, NULL, NULL, DEFAULT, 0, 3900, DEFAULT, NULL);

-- ============================================================
-- Initial data: order items
-- ============================================================
INSERT INTO order_items (id, quantity, order_id, item_id, price)
VALUES (seq_order_items.NEXTVAL, 1, 1, (SELECT id FROM items WHERE name = 'アライグマの精霊'), 8000);

INSERT INTO order_items (id, quantity, order_id, item_id, price)
VALUES (seq_order_items.NEXTVAL, 2, 1, (SELECT id FROM items WHERE name = '月光ベリー'), 1200);

INSERT INTO order_items (id, quantity, order_id, item_id, price)
VALUES (seq_order_items.NEXTVAL, 1, 2, (SELECT id FROM items WHERE name = 'キツネの精霊'), 12000);

INSERT INTO order_items (id, quantity, order_id, item_id, price)
VALUES (seq_order_items.NEXTVAL, 1, 2, (SELECT id FROM items WHERE name = '星屑クッキー'), 1800);

INSERT INTO order_items (id, quantity, order_id, item_id, price)
VALUES (seq_order_items.NEXTVAL, 1, 3, (SELECT id FROM items WHERE name = '【伝説級】始祖竜の精霊'), 300000);

INSERT INTO order_items (id, quantity, order_id, item_id, price)
VALUES (seq_order_items.NEXTVAL, 5, 3, (SELECT id FROM items WHERE name = '竜鱗ビスケット'), 18000);

-- ============================================================
-- Initial data: optional feature samples
-- ============================================================
INSERT INTO favorite (id, user_id, item_id, insert_date)
VALUES (seq_favorite.NEXTVAL, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), (SELECT id FROM items WHERE name = '【希少種】雲海ラッコの精霊'), DEFAULT);

INSERT INTO user_coupons (id, user_id, coupon_type_id, acquired_at, expires_at)
VALUES (seq_user_coupons.NEXTVAL, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), (SELECT id FROM coupon_types WHERE discount_rate = 10), DEFAULT, SYSTIMESTAMP + INTERVAL '30' DAY);

INSERT INTO login_histories (id, user_id, login_date_time, ip_address)
VALUES (seq_login_histories.NEXTVAL, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), SYSTIMESTAMP, '127.0.0.1');

INSERT INTO view_histories (id, user_id, item_id, view_date)
VALUES (seq_view_histories.NEXTVAL, (SELECT id FROM users WHERE email = 'ippan_saburo@test.co.jp'), (SELECT id FROM items WHERE name = '【希少種】雲海ラッコの精霊'), DEFAULT);

COMMIT;
