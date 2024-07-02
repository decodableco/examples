INSERT INTO t_i_orders
SELECT * 
	   FROM t_k_orders
	  WHERE cost > 100;
UNION ALL
SELECT ko.* 
       FROM t_k_orders ko
		    INNER JOIN 
		    customers c
		    ON ko.customerId = c.customerId
      WHERE c.vip_status = 'Gold';