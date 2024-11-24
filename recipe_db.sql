-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 24, 2024 at 12:43 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `recipe_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `recipes`
--

CREATE TABLE `recipes` (
  `code` varchar(10) NOT NULL,
  `name` varchar(255) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `tools` text DEFAULT NULL,
  `ingredients` text DEFAULT NULL,
  `steps` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `recipes`
--

INSERT INTO `recipes` (`code`, `name`, `category`, `tools`, `ingredients`, `steps`) VALUES
('R001', 'Caesar Salad', 'Salads', 'Bowl, Whisk', 'Lettuce, Caesar dressing, Croutons, Parmesan cheese', '1. Mix lettuce with Caesar dressing. \r\n2. Add croutons and Parmesan cheese. \r\n3. Serve chilled.'),
('R002', 'Spaghetti Carbonara', 'Main Course', 'Pot, Pan, Strainer', 'Spaghetti, Eggs, Bacon, Parmesan, Salt, Pepper', '1. Cook spaghetti. \r\n2. Fry bacon. \r\n3. Mix eggs with cheese. \r\n4. Combine all and serve.'),
('R003', 'Chocolate Cake', 'Desserts', 'Oven, Mixing bowl', 'Flour, Cocoa powder, Eggs, Butter, Sugar', '1. Preheat oven to 350°F. \r\n2. Mix ingredients. \r\n3. Bake for 25 minutes. \r\n4. Let cool and serve.'),
('R004', 'Tomato Soup', 'Soups', 'Pot, Blender', 'Tomatoes, Onion, Garlic, Broth, Salt, Pepper, Sugar', '1. Sauté onions and garlic. \r\n2. Add tomatoes and broth. \r\n3. Blend mixture. \r\n4. Serve hot....'),
('R005', 'Fruit Salads', 'Salads', 'Bowl', 'Apple, Orange, Banana, Grapes', '1. Chop fruit. \r\n2. Mix in a bowl. \r\n3. Serve chilled.'),
('R006', 'Spaghetti Bolognese', 'Main Course', 'Pot, Pan, Spatula, Strainer', 'Spaghetti, Ground Beef, Tomato Sauce, Onion, Garlic, Olive Oil, Salt, Pepper, Italian Seasoning', '1. Rebus spaghetti hingga al dente. 2. Tumis bawang dan bawang putih hingga harum. 3. Tambahkan daging. 4. Masukkan saus tomat dan bumbu. 5. Campur spaghetti dengan saus.'),
('R007', 'Pancake', 'Desserts', 'Bowl, Whisk, Frying Pan, Spatula', 'Flour, Sugar, Milk, Eggs, Butter, Baking Powder, Vanilla Extract, Salt', '1. Campur bahan kering di mangkuk. 2. Tambahkan bahan basah, aduk hingga rata. 3. Panaskan pan dan tuang adonan. 4. Masak hingga matang.'),
('R008', 'Fried Rice', 'Main Course', 'Wok, Spatula', 'Tomatoes, Onion, Garlic, Broth, Salt, Pepper', '1. Tumis bawang dan bawang putih. 2. Tambahkan ayam dan sayuran. 3. Masukkan nasi dan kecap. 4. Aduk hingga rata dan masak hingga matang.'),
('R009', 'Chocolate Cake', 'Desserts', 'Oven, Bowl, Whisk, Baking Pan', 'Flour, Sugar, Cocoa Powder, Baking Powder, Eggs, Milk, Butter, Vanilla Extract, Salt', '1. Campur bahan kering dan basah masing-masing. 2. Gabungkan adonan, tuang ke loyang. 3. Panggang di oven hingga matang.');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `recipes`
--
ALTER TABLE `recipes`
  ADD PRIMARY KEY (`code`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
