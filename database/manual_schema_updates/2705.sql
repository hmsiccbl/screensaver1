BEGIN;

INSERT INTO schema_history (screensaver_revision, date_updated, comment)
SELECT
2705,
current_timestamp,
'folded billing_information into screen';

alter table screen add column amount_to_be_charged_for_screen numeric(9, 2);
alter table screen add column billing_comments text;
alter table screen add column is_billing_for_supplies_only bool /*not null*/;
alter table screen add column billing_info_return_date date;
alter table screen add column date_charged date;
alter table screen add column date_completed5kcompounds date;
alter table screen add column date_faxed_to_billing_department date;
alter table screen add column facilities_and_administration_charge numeric(9, 2);
alter table screen add column is_fee_form_on_file bool /*not null*/;
alter table screen add column fee_form_requested_date date;
alter table screen add column fee_form_requested_initials text;
alter table screen add column see_comments bool /*not null*/;
alter table screen add column to_be_requested bool /*not null*/;

update screen set amount_to_be_charged_for_screen = bi.amount_to_be_charged_for_screen from billing_information bi where bi.screen_id = screen.screen_id;
update screen set billing_comments = bi.comments from billing_information bi where bi.screen_id = screen.screen_id;
update screen set is_billing_for_supplies_only = bi.is_billing_for_supplies_only from billing_information bi where bi.screen_id = screen.screen_id;
update screen set billing_info_return_date = bi.billing_info_return_date from billing_information bi where bi.screen_id = screen.screen_id;
update screen set date_charged = bi.date_charged from billing_information bi where bi.screen_id = screen.screen_id;
update screen set date_completed5kcompounds = bi.date_completed5kcompounds from billing_information bi where bi.screen_id = screen.screen_id;
update screen set date_faxed_to_billing_department = bi.date_faxed_to_billing_department from billing_information bi where bi.screen_id = screen.screen_id;
update screen set facilities_and_administration_charge = bi.facilities_and_administration_charge from billing_information bi where bi.screen_id = screen.screen_id;
update screen set is_fee_form_on_file = bi.is_fee_form_on_file from billing_information bi where bi.screen_id = screen.screen_id;
update screen set fee_form_requested_date = bi.fee_form_requested_date from billing_information bi where bi.screen_id = screen.screen_id;
update screen set fee_form_requested_initials = bi.fee_form_requested_initials from billing_information bi where bi.screen_id = screen.screen_id;
update screen set see_comments = bi.see_comments from billing_information bi where bi.screen_id = screen.screen_id;
update screen set to_be_requested = bi.to_be_requested from billing_information bi where bi.screen_id = screen.screen_id;

update screen set is_billing_for_supplies_only = false where is_billing_for_supplies_only is null;
update screen set is_fee_form_on_file = false where is_fee_form_on_file is null;
update screen set see_comments = false where see_comments is null;
update screen set to_be_requested = false where to_be_requested is null;

alter table screen alter column is_billing_for_supplies_only set not null;
alter table screen alter column is_fee_form_on_file set not null;
alter table screen alter column see_comments set not null;
alter table screen alter column to_be_requested set not null;

create table screen_billing_item (
    screen_id int4 not null,
    amount numeric(9, 2) not null,
    date_faxed date not null,
    item_to_be_charged text not null,
    primary key (screen_id, amount, date_faxed, item_to_be_charged)
);

alter table screen_billing_item 
    add constraint FK17BFCA69806C52FD 
    foreign key (screen_id) 
    references screen;

insert into screen_billing_item (screen_id, amount, date_faxed, item_to_be_charged) select bi.screen_id, i.amount, i.date_faxed, i.item_to_be_charged from billing_item i join billing_information bi using(billing_information_id);


drop table billing_item;
drop table billing_information;
drop sequence billing_information_id_seq;
drop sequence billing_item_id_seq;

COMMIT;
