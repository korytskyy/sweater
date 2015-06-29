# --- !Ups

create table sweat (
  id                        bigint not null,
  content                   varchar(255),
  owner_id                  bigint,
  creation_time             timestamp default CURRENT_TIMESTAMP ,
  constraint pk_sweat primary key (id))
;

create table user (
  id                        bigint not null,
  username                  varchar(255),
  password_hash             varchar(255),
  constraint uq_user_username unique (username),
  constraint pk_user primary key (id))
;

create sequence sweat_seq;

create sequence user_seq;

alter table sweat add constraint fk_sweat_owner_1 foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_sweat_owner_1 on sweat (owner_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists sweat;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists sweat_seq;

drop sequence if exists user_seq;

