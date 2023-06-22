EXECUTE STATEMENT SET
BEGIN

INSERT INTO orders_with_lines_kafka
  SELECT
    po.id,
    po.order_date,
    po.purchaser_id,
    ( SELECT ARRAY_AGGR(ROW(ol.id, ol.product_id, ol.quantity, ol.price))
      FROM order_lines ol
      WHERE ol.order_id = po.id )
  FROM
    purchase_orders po;

INSERT INTO orders_with_lines_es
  SELECT
    po.id,
    po.order_date,
    po.purchaser_id,
    ( SELECT ARRAY_AGGR(ROW(ol.id, ol.product_id, ol.quantity, ol.price))
      FROM order_lines ol
      WHERE ol.order_id = po.id )
  FROM
    purchase_orders po;

INSERT INTO orders_with_lines_and_customer_kafka
  SELECT
    po.id,
    po.order_date,
    ROW(
      c.id,
      c.first_name,
      c.last_name,
      c.email,
      ( SELECT ARRAY_AGGR(ROW(cpn.id, cpn.type, cpn.`value`))
        FROM customer_phone_numbers cpn
        WHERE cpn.customer_id = c.id )
    ),
    ( SELECT ARRAY_AGGR(ROW(ol.id, ol.product_id, ol.quantity, ol.price))
      FROM order_lines ol
      WHERE ol.order_id = po.id )
FROM
    purchase_orders po
    JOIN customers c ON po.purchaser_id = c.id;

INSERT INTO orders_with_lines_and_customer_es
  SELECT
    po.id,
    po.order_date,
    ROW(
      c.id,
      c.first_name,
      c.last_name,
      c.email,
      ( SELECT ARRAY_AGGR(ROW(cpn.id, cpn.type, cpn.`value`))
        FROM customer_phone_numbers cpn
        WHERE cpn.customer_id = c.id )
    ),
    ( SELECT ARRAY_AGGR(ROW(ol.id, ol.product_id, ol.quantity, ol.price))
      FROM order_lines ol
      WHERE ol.order_id = po.id )
FROM
    purchase_orders po
    JOIN customers c ON po.purchaser_id = c.id;

END;