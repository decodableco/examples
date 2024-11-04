alter table customers
alter column customer_id set not null;
alter table customers
add constraint pk_customers primary key (customer_id);

alter table pets
alter column pet_id set not null;
alter table pets
add constraint pk_pets primary key (pet_id);

alter table appointments
alter column appointment_id set not null;
alter table appointments
add constraint pk_appointments primary key (appointment_id );