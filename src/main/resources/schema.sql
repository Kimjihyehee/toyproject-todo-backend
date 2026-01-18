-- Make deleted_timestamp nullable for soft-delete semantics
ALTER TABLE IF EXISTS todo_user
    ALTER COLUMN deleted_timestamp DROP NOT NULL;
