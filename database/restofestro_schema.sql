-- RestoFestro database: schema + sample data
DROP DATABASE IF EXISTS restofestro;
CREATE DATABASE restofestro;
USE restofestro;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    password VARCHAR(64) NOT NULL          -- SHA-256 hash (64 hex chars)
);

CREATE TABLE admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL
);

CREATE TABLE food (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(8,2) NOT NULL,
    image VARCHAR(150),
    veg_type ENUM('Veg','Non-Veg') NOT NULL,
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (food_id) REFERENCES food(id)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(30) DEFAULT 'Confirmed',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (food_id) REFERENCES food(id)
);

-- Default admin (email: admin@restofestro.com, password: admin123)
INSERT INTO admins (email, password)
VALUES ('admin@restofestro.com', SHA2('admin123', 256));

-- Sample food data: 6 dishes per state
INSERT INTO food (name, state, category, description, price, image, veg_type) VALUES
-- Andhra Pradesh
('Pesarattu',        'Andhra Pradesh', 'Breakfast', 'Green gram dosa served with ginger chutney', 90,  'images/pesarattu.jpg',   'Veg'),
('Andhra Meals',     'Andhra Pradesh', 'Meals',     'Full meal with rice, dal, curries and pickle', 180, 'images/andhra-meals.jpg', 'Veg'),
('Pulihora',         'Andhra Pradesh', 'Meals',     'Tangy tamarind rice with peanuts',             100, 'images/pulihora.jpg',    'Veg'),
('Gongura Chicken',  'Andhra Pradesh', 'Curries',   'Chicken cooked with sorrel leaves',            260, 'images/gongura-chicken.jpg', 'Non-Veg'),
('Gutti Vankaya',    'Andhra Pradesh', 'Curries',   'Stuffed brinjal curry in spicy masala',        150, 'images/gutti-vankaya.jpg', 'Veg'),
('Punugulu',         'Andhra Pradesh', 'Snacks',    'Crispy fried batter balls with chutney',       70,  'images/punugulu.jpg',    'Veg'),

-- Telangana
('Sarva Pindi',      'Telangana', 'Breakfast',   'Rice flour pancake with peanuts and spices',   80,  'images/sarva-pindi.jpg', 'Veg'),
('Telangana Meals',  'Telangana', 'Main Course', 'Traditional meal with roti, rice and curries', 190, 'images/telangana-meals.jpg', 'Veg'),
('Jonna Roti',       'Telangana', 'Main Course', 'Sorghum flatbread with curry',                 110, 'images/jonna-roti.jpg',  'Veg'),
('Hyderabadi Biryani','Telangana','Main Course', 'Layered basmati rice with spiced chicken',     280, 'images/biryani.jpg',     'Non-Veg'),
('Bagara Baingan',   'Telangana', 'Curries',     'Brinjal in peanut and sesame gravy',           160, 'images/bagara-baingan.jpg', 'Veg'),
('Sakinalu',         'Telangana', 'Snacks',      'Crunchy spiral rice flour snack',              60,  'images/sakinalu.jpg',    'Veg'),

-- Karnataka
('Mysore Masala Dosa','Karnataka', 'Breakfast',   'Crisp dosa with red chutney and potato masala', 120, 'images/mysore-dosa.jpg', 'Veg'),
('Rava Idli',        'Karnataka', 'Breakfast',   'Soft semolina idli with sambar',               80,  'images/rava-idli.jpg',   'Veg'),
('Bisi Bele Bath',   'Karnataka', 'Main Course', 'Spicy rice and lentil one-pot dish',           130, 'images/bisi-bele-bath.jpg', 'Veg'),
('Karnataka Meals',  'Karnataka', 'Main Course', 'Full meal with rice, sambar, palya and payasa', 170, 'images/karnataka-meals.jpg', 'Veg'),
('Neer Dosa',        'Karnataka', 'Specials',    'Thin lacy rice dosa with coconut chutney',     90,  'images/neer-dosa.jpg',   'Veg'),
('Ragi Mudde',       'Karnataka', 'Specials',    'Finger millet balls served with saaru',        110, 'images/ragi-mudde.jpg',  'Veg'),

-- Tamil Nadu
('Idli Sambar',      'Tamil Nadu', 'Breakfast', 'Steamed rice cakes with sambar and chutney',   70,  'images/idli.jpg',        'Veg'),
('Ven Pongal',       'Tamil Nadu', 'Breakfast', 'Rice and moong dal with pepper and ghee',      90,  'images/pongal.jpg',      'Veg'),
('Tamil Meals',      'Tamil Nadu', 'Meals',     'Traditional meal with sambar, rasam and poriyal', 170, 'images/tamil-meals.jpg', 'Veg'),
('Lemon Rice',       'Tamil Nadu', 'Meals',     'Tangy rice tempered with peanuts',             90,  'images/lemon-rice.jpg',  'Veg'),
('Kothu Parotta',    'Tamil Nadu', 'Specials',  'Shredded parotta stir-fried with egg and salna', 160, 'images/kothu-parotta.jpg', 'Non-Veg'),
('Chettinad Chicken','Tamil Nadu', 'Specials',  'Fiery chicken curry with freshly ground spices', 270, 'images/chettinad-chicken.jpg', 'Non-Veg'),

-- Kerala
('Appam with Stew',  'Kerala', 'Breakfast', 'Lacy rice hoppers with vegetable stew',            110, 'images/appam.jpg',       'Veg'),
('Puttu and Kadala', 'Kerala', 'Breakfast', 'Steamed rice cylinders with black chickpea curry', 100, 'images/puttu.jpg',       'Veg'),
('Idiyappam',        'Kerala', 'Breakfast', 'String hoppers served with coconut milk',          90,  'images/idiyappam.jpg',   'Veg'),
('Kerala Sadya',     'Kerala', 'Meals',     'Feast on banana leaf with many traditional dishes', 220, 'images/sadya.jpg',     'Veg'),
('Kerala Fish Curry','Kerala', 'Specials',  'Fish in tangy coconut and kokum gravy',            240, 'images/fish-curry.jpg',  'Non-Veg'),
('Malabar Biryani',  'Kerala', 'Specials',  'Fragrant biryani with chicken and fried onions',   270, 'images/malabar-biryani.jpg', 'Non-Veg');

-- Quick check
SELECT state, COUNT(*) AS dishes FROM food GROUP BY state;
