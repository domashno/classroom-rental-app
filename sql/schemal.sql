CREATE TABLE IF NOT EXISTS classrooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL CHECK (capacity > 0),
    equipment VARCHAR(255) NOT NULL DEFAULT '',
    accessible BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('COMPANY', 'INDIVIDUAL')),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    organization_number VARCHAR(50),
    contact_person VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS booking_users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    department VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    classroom_id INT NOT NULL REFERENCES classrooms(id) ON DELETE RESTRICT,
    customer_id INT NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    booking_user_id INT NOT NULL REFERENCES booking_users(id) ON DELETE RESTRICT,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    comments TEXT,
    CHECK (end_time > start_time)
);

-- Seed 20 training classrooms of varying sizes, equipment, and accessibility
INSERT INTO classrooms (name, capacity, equipment, accessible) VALUES
    ('Room A1 - Oak', 12, 'Projector,Whiteboard', TRUE),
    ('Room A2 - Birch', 12, 'Whiteboard', TRUE),
    ('Room A3 - Pine', 16, 'Projector,Whiteboard,Speakers', FALSE),
    ('Room A4 - Cedar', 16, 'Projector,Whiteboard', TRUE),
    ('Room B1 - Maple', 20, 'Projector,Whiteboard,Video Conference', TRUE),
    ('Room B2 - Elm', 20, 'Projector,Whiteboard', FALSE),
    ('Room B3 - Spruce', 24, 'Projector,Dual Whiteboards,Speakers', TRUE),
    ('Room B4 - Ash', 24, 'Whiteboard,Flipchart', FALSE),
    ('Room C1 - Horizon', 30, 'Projector,Microphones,Speakers,Whiteboard', TRUE),
    ('Room C2 - Summit', 30, 'Projector,Video Conference,Whiteboard', TRUE),
    ('Room C3 - Beacon', 35, 'Dual Projectors,Whiteboard,Speakers', TRUE),
    ('Room C4 - Harbor', 35, 'Projector,Whiteboard', FALSE),
    ('Room D1 - Aurora', 40, 'Dual Projectors,Microphones,Speakers,Whiteboard', TRUE),
    ('Room D2 - Solstice', 40, 'Projector,Video Conference,Microphones', TRUE),
    ('Room D3 - Meridian', 50, 'Dual Projectors,Microphones,Speakers,Stage', TRUE),
    ('Room D4 - Zenith', 50, 'Projector,Microphones,Speakers,Whiteboard', FALSE),
    ('Lab E1 - Turing', 18, 'Workstations,Projector,Whiteboard', TRUE),
    ('Lab E2 - Lovelace', 18, 'Workstations,Projector,Whiteboard', TRUE),
    ('Lab E3 - Hopper', 22, 'Workstations,Dual Projectors,Whiteboard', FALSE),
    ('Auditorium F1 - Forum', 80, 'Dual Projectors,Stage,Microphones,Speakers,Streaming', TRUE)
ON CONFLICT (name) DO NOTHING;

-- Seed default booking user
INSERT INTO booking_users (name, department) VALUES
    ('Alex Lindqvist', 'Facility Operations'),
    ('Samira Haddad', 'Client Bookings')
ON CONFLICT DO NOTHING;