ALTER TABLE omd_customers REPLICA IDENTITY FULL;
ALTER TABLE omd_pets REPLICA IDENTITY FULL;
ALTER TABLE omd_appointments REPLICA IDENTITY FULL;

SELECT oid::regclass, relreplident FROM pg_class
 WHERE oid in ( 'omd_customers'::regclass, 'omd_pets'::regclass, 'omd_appointments'::regclass);
