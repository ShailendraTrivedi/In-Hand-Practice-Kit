-- Insert default roles if they don't exist
INSERT INTO roles (role_name, role_description)
VALUES ('ADMIN', 'Administrator with full access')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name, role_description)
VALUES ('USER', 'Regular user with limited access')
ON CONFLICT (role_name) DO NOTHING;

