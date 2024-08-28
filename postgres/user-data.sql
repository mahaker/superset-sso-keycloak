-- user_type = (0: student, 1: teacher)
CREATE TABLE users (
 user_id BIGSERIAL NOT NULL PRIMARY KEY,
 user_cd NCHAR VARYING(20) NOT NULL,
 user_name NCHAR VARYING(50) NOT NULL,
 user_password NCHAR VARYING(256) NOT NULL,
 user_type INTEGER NOT NULL
);

INSERT INTO
  users(user_cd, user_name, user_password, user_type)
VALUES
  ('st_001', 'ST-001', '', 0)
, ('st_002', 'ST-002', '', 0)
, ('st_003', 'ST-003', '', 0)
, ('te_001', 'TE-001', '', 1)
, ('te_002', 'TE-002', '', 1)
;

-- user_password = 'password001'
UPDATE
  users
SET
  user_password = '04312bf5b08ace478c3de27d332ef987b5693b86bd8fcd75aad790c67f8748dfc9428d0a014bd68aba086a0cae22c9cbe746d6dcf35ac7d3ee2a0f1e34191e21'
;
