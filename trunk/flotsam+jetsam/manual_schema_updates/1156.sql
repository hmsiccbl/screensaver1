ALTER TABLE library_screening ADD COLUMN is_special BOOL;
ALTER TABLE library_screening ALTER COLUMN is_special SET NOT NULL;
