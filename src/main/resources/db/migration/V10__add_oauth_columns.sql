ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(32);
ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_subject VARCHAR(512);

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_oauth_provider_subject
    ON users (oauth_provider, oauth_subject)
    WHERE oauth_provider IS NOT NULL AND oauth_subject IS NOT NULL;
