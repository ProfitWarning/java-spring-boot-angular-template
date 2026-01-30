-- Add created_at column to testmessages table
ALTER TABLE testmessages 
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
