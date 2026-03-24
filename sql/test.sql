select * from user;
select * from user where is_deleted = 0;
insert into user(username, password) values ('tom', '123456');

select count(1) from user where username = 'tom';