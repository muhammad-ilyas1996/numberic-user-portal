-- Create portal_types table if it doesn't exist
CREATE TABLE IF NOT EXISTS portal_types (
                                            portal_type_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            portal_name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_ON TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MODIFIED_ON TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CREATED_BY VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    MODIFIED_BY VARCHAR(255) NOT NULL DEFAULT 'SYSTEM'
    );


INSERT INTO portal_types (portal_type_id, portal_name, display_name, description, IS_ACTIVE, CREATED_ON, MODIFIED_ON, CREATED_BY, MODIFIED_BY) VALUES
(1, 'SUPER_ADMIN_PORTAL', 'Super Admin Portal', 'Portal for super administrators to manage both portals', true, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
(2, 'PM_PORTAL', 'PM Portal', 'Portal for practice management users', true, NOW(), NOW(), 'SYSTEM', 'SYSTEM');
ALTER TABLE roles
    ADD COLUMN portal_type_id BIGINT NOT NULL DEFAULT 1;

ALTER TABLE permissions ADD CONSTRAINT fk_permissions_portal_type FOREIGN KEY (portal_type_id) REFERENCES portal_types(portal_type_id);
ALTER TABLE roles ADD CONSTRAINT fk_roles_portal_type FOREIGN KEY (portal_type_id) REFERENCES portal_types(portal_type_id);

ALTER TABLE permissions

    ADD COLUMN CREATED_ON TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN MODIFIED_ON TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                                                                                               ADD COLUMN CREATED_BY VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
                                                                                                                               ADD COLUMN MODIFIED_BY VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
                                                                                                                               ADD COLUMN portal_type_id BIGINT NOT NULL DEFAULT 1;

ALTER TABLE roles
DROP COLUMN updated_by,
DROP COLUMN updated_on;

ALTER TABLE roles DROP FOREIGN KEY roles_ibfk_1;
ALTER TABLE roles
    MODIFY COLUMN created_by VARCHAR(255),


ALTER TABLE roles DROP FOREIGN KEY roles_ibfk_1;
ALTER TABLE roles
    ADD COLUMN created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,



