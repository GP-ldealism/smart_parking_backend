select * from user;
select * from user where is_deleted = 0;
insert into user(username, password) values ('tom', '123456');

select count(1) from user where username = 'tom';

select * from parking_order where user_id = 2 and order_no = 'PK20260427195720F43685B5';
select * from parking_order where order_no = 'PK20260422232711AB30940A';

select count(*) from parking_order where user_id = 2;
select count(*) from parking_order where user_id = 2 and status = 0;
select * from parking_order where order_no like '%85B5';
select * from parking_order where order_no = 'PK202604272051028DC09652';