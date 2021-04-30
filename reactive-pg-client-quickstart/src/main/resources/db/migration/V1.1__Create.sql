CREATE TABLE template (
  id UUID NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  last_change TIMESTAMP NOT NULL,
  subject TEXT NOT NULL,
  plain TEXT NOT NULL,
  html TEXT NOT NULL,

  UNIQUE (name, last_change)
);

CREATE TABLE content (
  template_id UUID NOT NULL REFERENCES template (id) ON DELETE CASCADE,
  cid TEXT NOT NULL,
  bytes bytea NOT NULL,
  mime_type TEXT NOT NULL,

  PRIMARY KEY (template_id, cid)
);
