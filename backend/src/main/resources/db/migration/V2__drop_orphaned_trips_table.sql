-- Drop orphaned trips table from previous schema version
-- This table is not mapped to any current JPA entity and causes FK constraint
-- violations when cleaning up test users.

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `trips`;
SET FOREIGN_KEY_CHECKS = 1;