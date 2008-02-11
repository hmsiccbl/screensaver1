BEGIN;

UPDATE screensaver_revision SET screensaver_revision=1401;
 
CREATE TABLE copy_info_microliter_well_volumes (
    copy_info_id int4 NOT NULL,
    microliter_volume NUMERIC(19, 2),
    well_key varchar(255) NOT NULL,
    PRIMARY KEY (copy_info_id, well_key)
);

ALTER TABLE copy_info_microliter_well_volumes 
    ADD CONSTRAINT FKD660F2C0F774E976 
    FOREIGN KEY (copy_info_id) 
    REFERENCES copy_info;

ALTER TABLE copy_info RENAME volume TO microliter_well_volume;


COMMIT;
