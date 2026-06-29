DECLARE
  TYPE user_id_table IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
  v_user_ids user_id_table;

  v_order_id NUMBER(6);
  v_order_item_id NUMBER(6);
  v_idx NUMBER := 0;

  PROCEDURE add_review(
    p_user_id IN NUMBER,
    p_item_id IN NUMBER,
    p_item_price IN NUMBER,
    p_rating IN NUMBER,
    p_comment IN VARCHAR2,
    p_days_ago IN NUMBER
  ) IS
  BEGIN
    INSERT INTO orders (
      id, postal_code, address, name, phone_number,
      pay_method, user_id, insert_date, delivery_date
    ) VALUES (
      seq_orders.NEXTVAL,
      '1600001',
      '東京都新宿区レビュー町1-2-3',
      'レビュー用会員',
      '09011112222',
      2,
      p_user_id,
      TRUNC(SYSDATE) - p_days_ago,
      TRUNC(SYSDATE) - p_days_ago + 3
    )
    RETURNING id INTO v_order_id;

    INSERT INTO order_items (
      id, quantity, order_id, item_id, price
    ) VALUES (
      seq_order_items.NEXTVAL,
      1,
      v_order_id,
      p_item_id,
      p_item_price
    )
    RETURNING id INTO v_order_item_id;

    INSERT INTO reviews (
      id, user_id, item_id, order_item_id,
      rating, review_comment, insert_date
    ) VALUES (
      seq_reviews.NEXTVAL,
      p_user_id,
      p_item_id,
      v_order_item_id,
      p_rating,
      p_comment,
      SYSTIMESTAMP - NUMTODSINTERVAL(p_days_ago, 'DAY')
    );
  END;
BEGIN
  -- 再実行用：このSQLで作るレビュー用会員の注文・レビューだけ削除
  DELETE FROM reviews
  WHERE user_id IN (
    SELECT id FROM users
    WHERE email IN (
      'review_haruka@test.co.jp',
      'review_kenta@test.co.jp',
      'review_emily@test.co.jp',
      'review_minseo@test.co.kr',
      'review_ling@test.cn'
    )
  );

  DELETE FROM order_items
  WHERE order_id IN (
    SELECT o.id FROM orders o
    JOIN users u ON o.user_id = u.id
    WHERE u.email IN (
      'review_haruka@test.co.jp',
      'review_kenta@test.co.jp',
      'review_emily@test.co.jp',
      'review_minseo@test.co.kr',
      'review_ling@test.cn'
    )
  );

  DELETE FROM orders
  WHERE user_id IN (
    SELECT id FROM users
    WHERE email IN (
      'review_haruka@test.co.jp',
      'review_kenta@test.co.jp',
      'review_emily@test.co.jp',
      'review_minseo@test.co.kr',
      'review_ling@test.cn'
    )
  );

  DELETE FROM users
  WHERE email IN (
    'review_haruka@test.co.jp',
    'review_kenta@test.co.jp',
    'review_emily@test.co.jp',
    'review_minseo@test.co.kr',
    'review_ling@test.cn'
  );

  INSERT INTO users (
    id, email, password, name, postal_code, address,
    phone_number, authority, delete_flag, insert_date,
    login_failure_count, lock_release_time, point
  ) VALUES (
    seq_users.NEXTVAL, 'review_haruka@test.co.jp', 'Testtest3', '佐藤はるか',
    '1600001', '東京都新宿区レビュー町1-2-3',
    '09011112222', 2, 0, SYSDATE, 0, NULL, 300
  ) RETURNING id INTO v_user_ids(1);

  INSERT INTO users VALUES (
    seq_users.NEXTVAL, 'review_kenta@test.co.jp', 'Testtest4', '田中健太',
    '1500001', '東京都渋谷区レビュー坂4-5-6',
    '09022223333', 2, 0, SYSDATE, 0, NULL, 450
  ) RETURNING id INTO v_user_ids(2);

  INSERT INTO users VALUES (
    seq_users.NEXTVAL, 'review_emily@test.co.jp', 'Testtest5', 'Emily Carter',
    '5300001', '大阪府大阪市北区レビュー通7-8-9',
    '09033334444', 2, 0, SYSDATE, 0, NULL, 200
  ) RETURNING id INTO v_user_ids(3);

  INSERT INTO users VALUES (
    seq_users.NEXTVAL, 'review_minseo@test.co.kr', 'Testtest6', 'Kim Minseo',
    '4600001', '愛知県名古屋市レビュー町2-4-6',
    '09044445555', 2, 0, SYSDATE, 0, NULL, 180
  ) RETURNING id INTO v_user_ids(4);

  INSERT INTO users VALUES (
    seq_users.NEXTVAL, 'review_ling@test.cn', 'Testtest7', 'Wang Ling',
    '8100001', '福岡県福岡市レビュー町8-8-8',
    '09055556666', 2, 0, SYSDATE, 0, NULL, 260
  ) RETURNING id INTO v_user_ids(5);

  FOR item_rec IN (
    SELECT id, name, price
    FROM items
    WHERE delete_flag = 0
    ORDER BY id
  ) LOOP
    v_idx := v_idx + 1;

    add_review(
      v_user_ids(1),
      item_rec.id,
      item_rec.price,
      CASE MOD(v_idx, 4) WHEN 0 THEN 5 WHEN 1 THEN 4 WHEN 2 THEN 5 ELSE 4 END,
      item_rec.name || '、届いた日から棚の主みたいな顔をしています。眺めるたびに少し元気が出ます。',
      40 + v_idx
    );

    add_review(
      v_user_ids(2),
      item_rec.id,
      item_rec.price,
      CASE MOD(v_idx, 4) WHEN 0 THEN 4 WHEN 1 THEN 4 WHEN 2 THEN 3 ELSE 5 END,
      item_rec.name || 'は写真より表情がやわらかくて気に入りました。存在感は強めですが、部屋が物語っぽくなります。',
      20 + v_idx
    );

    IF MOD(v_idx, 3) = 0 THEN
      add_review(
        v_user_ids(3),
        item_rec.id,
        item_rec.price,
        CASE MOD(v_idx, 4) WHEN 0 THEN 4 WHEN 1 THEN 3 WHEN 2 THEN 4 ELSE 4 END,
        'Bought ' || item_rec.name || ' as a souvenir. It has a quiet charm, though it seems to judge my coffee choices.',
        10 + v_idx
      );
    ELSIF MOD(v_idx, 3) = 1 THEN
      add_review(
        v_user_ids(4),
        item_rec.id,
        item_rec.price,
        CASE MOD(v_idx, 4) WHEN 0 THEN 4 WHEN 1 THEN 3 WHEN 2 THEN 4 ELSE 4 END,
        item_rec.name || ' 포장이 예쁘고 분위기가 좋아요. 밤에는 존재감이 조금 강해서 별 하나 뺐습니다.',
        10 + v_idx
      );
    ELSE
      add_review(
        v_user_ids(5),
        item_rec.id,
        item_rec.price,
        CASE MOD(v_idx, 4) WHEN 0 THEN 4 WHEN 1 THEN 3 WHEN 2 THEN 4 ELSE 4 END,
        item_rec.name || ' 摆在书桌旁很有陪伴感，做工细致，就是偶尔太抢镜。',
        10 + v_idx
      );
    END IF;
  END LOOP;

  COMMIT;
END;
/