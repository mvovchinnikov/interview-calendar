CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE booking_status AS ENUM ('NOT_APPROVED', 'APPROVED', 'DECLINED');
CREATE TYPE hr_role AS ENUM ('HR1', 'HR2');

CREATE TABLE app_user (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  role TEXT NOT NULL CHECK (role IN ('DEV','HR1','HR2')),
  display_name TEXT NOT NULL,
  email TEXT NOT NULL,
  telegram_chat_id TEXT,
  public_token TEXT UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE event_type (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  developer_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  name TEXT NOT NULL CHECK (char_length(name) <= 18),
  UNIQUE (developer_id, LOWER(name))
);

CREATE TABLE availability_slot (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  developer_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  start_at TIMESTAMPTZ NOT NULL,
  duration_minutes INT NOT NULL CHECK (duration_minutes = 30),
  UNIQUE (developer_id, start_at)
);

CREATE TABLE booking (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  developer_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  created_by_role hr_role NOT NULL,
  event_type_name TEXT NOT NULL CHECK (char_length(event_type_name) <= 18),
  start_at TIMESTAMPTZ NOT NULL,
  duration_minutes INT NOT NULL CHECK (duration_minutes IN (30,60,90,120)),
  status booking_status NOT NULL DEFAULT 'NOT_APPROVED',
  company TEXT NOT NULL,
  hr_name TEXT NOT NULL,
  hr_email TEXT NOT NULL,
  meeting_link TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (developer_id, start_at)
);

WITH dev AS (
  INSERT INTO app_user (id, role, display_name, email, public_token)
  VALUES ('11111111-1111-1111-1111-111111111111', 'DEV', 'Demo Developer', 'dev@example.com', 'demo-token')
  RETURNING id
)
INSERT INTO event_type (developer_id, name)
SELECT dev.id, event_name
FROM dev,
  (VALUES ('Screening'), ('Technical'), ('HR Manager')) AS seed(event_name);
