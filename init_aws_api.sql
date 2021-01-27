create table db_user(username varchar(256), pw_hash varchar(2048), salt varchar(2048));
create unique index idx2541054d on db_user(username);
insert into db_user values('admin', '8Sw8nanvihUNOCyY1qOOxg587HwivE2MA+dgXyscACfbPx/jd6n9IEnhdPpyzDX/BWf+4odC3ozCCuCaui80AQ==', '6i69ttZEMjfa/10NfI29jQ==');