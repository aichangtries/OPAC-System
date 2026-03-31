-- java -cp ".;mysql-connector-j-9.6.0.jar" OPACSystem -- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 18, 2026 at 12:11 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `opac_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `books`
--

CREATE TABLE `books` (
  `book_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `author` varchar(255) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `dewey_decimal` varchar(50) DEFAULT NULL,
  `is_available` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `borrowers`
--

CREATE TABLE `borrowers` (
  `borrower_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `transaction_id` int(11) NOT NULL,
  `book_id` int(11) DEFAULT NULL,
  `borrower_id` int(11) DEFAULT NULL,
  `borrow_date` date DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `return_date` date DEFAULT NULL,
  `overdue_fee` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `payment_id` int(11) NOT NULL,
  `borrower_id` int(11) NOT NULL,
  `transaction_id` int(11) DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `paid_at` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `dewey_classifications`
--

CREATE TABLE `dewey_classifications` (
  `classification_id` int(11) NOT NULL,
  `code` varchar(20) NOT NULL UNIQUE,
  `description` varchar(255) NOT NULL,
  `keywords` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `dewey_classifications`
--

--
-- Dumping data for table `books`
--

INSERT INTO books (title, author, category, dewey_decimal) VALUES
('The Silent Forest', 'Maria Santos', 'Fiction', '800 - Literature'),
('Ocean of Stars', 'Daniel Cruz', 'Science Fiction', '800 - Literature'),
('Love Beyond Time', 'Angela Reyes', 'Romance', '800 - Literature'),
('The Hidden Temple', 'Carlos Mendoza', 'Adventure', '900 - History & Geography'),
('Basics of Programming', 'John Lim', 'Technology', '000 - Computer Science, Information & General'),
('World Atlas for Kids', 'Unknown', 'Geography', '900 - History & Geography'),
('Filipino Folk Tales', 'Liza Ramos', 'Children''s Story', '800 - Literature'),
('The Last Warrior', 'Miguel Torres', 'Fiction', '800 - Literature'),
('Introduction to Psychology', 'Dr. Helen Yu', 'Psychology', '100 - Philosophy & Psychology'),
('Earth and Life Science', 'Peter Gonzales', 'Science', '500 - Science'),
('Cooking Made Easy', 'Chef Ramon', 'Cooking', '600 - Technology & Applied Sciences'),
('The Great Adventure', 'Anna Lopez', 'Children''s Story', '800 - Literature'),
('Philippine History Simplified', 'Jose Bautista', 'History', '900 - History & Geography'),
('Algebra Essentials', 'Mark David', 'Mathematics', '500 - Science'),
('Creative Writing Guide', 'Sofia Perez', 'Education', '000 - Computer Science, Information & General'),
('Digital Marketing 101', 'Kevin Tan', 'Business', '300 - Social Sciences'),
('The Mystery Island', 'Carla Dizon', 'Mystery', '800 - Literature'),
('Space Exploration', 'Neil Ramirez', 'Science', '500 - Science'),
('Human Anatomy Basics', 'Dr. Luis Garcia', 'Medicine', '600 - Technology & Applied Sciences'),
('English Grammar Made Simple', 'Patricia Ong', 'Education', '400 - Language'),
('The Lost City', 'Marco Villanueva', 'Adventure', '900 - History & Geography'),
('Ethics and Society', 'Fr. Antonio Cruz', 'Philosophy', '100 - Philosophy & Psychology'),
('Basic Economics', 'Robert Chan', 'Economics', '300 - Social Sciences'),
('Photography for Beginners', 'Alex Chua', 'Arts', '700 - Arts & Recreation'),
('The Enchanted Garden', 'Nina Flores', 'Children''s Story', '800 - Literature'),
('Music Theory Basics', 'Carla Santos', 'Music', '700 - Arts & Recreation'),
('The Science of Climate', 'Victor Reyes', 'Science', '500 - Science'),
('Business Management Principles', 'Henry Co', 'Business', '300 - Social Sciences'),
('The Time Traveler', 'Ian Cortez', 'Science Fiction', '800 - Literature'),
('Introduction to Philosophy', 'Angela Cruz', 'Philosophy', '100 - Philosophy & Psychology');

--
-- Dumping data for table `borrowers`
--

INSERT INTO `borrowers` (`borrower_id`, `name`) VALUES
(1, 'John Doe'),
(2, 'Jane Smith'),
(3, 'Michael Johnson');

--
-- Dumping data for table `transactions`
--

INSERT INTO `transactions` (`transaction_id`, `book_id`, `borrower_id`, `borrow_date`, `due_date`, `return_date`, `overdue_fee`) VALUES
(1, 4, 1, '2026-03-15', '2026-03-29', NULL, 0.00);

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`payment_id`, `borrower_id`, `transaction_id`, `amount`, `remarks`, `paid_at`) VALUES
(1, 1, 1, 50.00, 'Initial partial payment', '2026-03-30 09:30:00');

--
-- Dumping data for table `dewey_classifications`
--

INSERT INTO `dewey_classifications` (`code`, `description`, `keywords`) VALUES
('000', 'General Works', 'general, reference'),
('005', 'Computers & Information', 'computer, programming, it, technology, software, code'),
('100', 'Philosophy & Psychology', 'philosophy, psychology, mind, thought'),
('200', 'Religion', 'religion, theology, spirituality, faith, bible'),
('300', 'Social Sciences', 'social, politics, government, law, education, business'),
('400', 'Language', 'language, english, grammar, linguistics'),
('500', 'Science', 'science, physics, chemistry, biology, nature, math, mathematics, astronomy'),
('600', 'Technology & Applied Sciences', 'engineering, medical, health, cooking, agriculture, industry'),
('700', 'Arts & Recreation', 'art, music, painting, sculpture, sports, games, entertainment'),
('800', 'Literature', 'literature, fiction, novel, poetry, drama, story'),
('900', 'History & Geography', 'history, geography, travel, biography');

-- --------------------------------------------------------

-- Helper view to summarize overdue balances after payments
CREATE OR REPLACE VIEW `borrower_fee_summary` AS
SELECT
  b.borrower_id,
  b.name,
  COALESCE(SUM(
    CASE
      WHEN t.due_date IS NULL THEN 0
      WHEN t.return_date IS NULL OR t.return_date > t.due_date
        THEN GREATEST(DATEDIFF(COALESCE(t.return_date, CURDATE()), t.due_date), 0) * 50
      ELSE 0
    END
  ), 0) AS total_overdue,
  COALESCE(p.total_paid, 0) AS total_paid,
  GREATEST(
    COALESCE(SUM(
      CASE
        WHEN t.due_date IS NULL THEN 0
        WHEN t.return_date IS NULL OR t.return_date > t.due_date
          THEN GREATEST(DATEDIFF(COALESCE(t.return_date, CURDATE()), t.due_date), 0) * 50
        ELSE 0
      END
    ), 0) - COALESCE(p.total_paid, 0),
    0
  ) AS outstanding
FROM borrowers b
LEFT JOIN transactions t ON t.borrower_id = b.borrower_id
LEFT JOIN (
  SELECT borrower_id, SUM(amount) AS total_paid
  FROM payments
  GROUP BY borrower_id
) p ON p.borrower_id = b.borrower_id
GROUP BY b.borrower_id, b.name;

-- Run `SELECT * FROM borrower_fee_summary;` to see net balances per member.

--
-- Indexes for dumped tables
--

--
-- Indexes for table `books`
--
ALTER TABLE `books`
  ADD PRIMARY KEY (`book_id`);

--
-- Indexes for table `borrowers`
--
ALTER TABLE `borrowers`
  ADD PRIMARY KEY (`borrower_id`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `book_id` (`book_id`),
  ADD KEY `borrower_id` (`borrower_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`payment_id`),
  ADD KEY `payments_borrower_id_idx` (`borrower_id`),
  ADD KEY `payments_transaction_id_idx` (`transaction_id`);

--
-- Indexes for table `dewey_classifications`
--
ALTER TABLE `dewey_classifications`
  ADD PRIMARY KEY (`classification_id`);

--
-- FOREIGN KEY Constraints
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`borrower_id`) REFERENCES `borrowers` (`borrower_id`) ON DELETE CASCADE;

ALTER TABLE `payments`
  ADD CONSTRAINT `payments_borrower_fk` FOREIGN KEY (`borrower_id`) REFERENCES `borrowers` (`borrower_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `payments_transaction_fk` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`transaction_id`) ON DELETE SET NULL;

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `books`
--
ALTER TABLE `books`
  MODIFY `book_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `borrowers`
--
ALTER TABLE `borrowers`
  MODIFY `borrower_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `dewey_classifications`
--
ALTER TABLE `dewey_classifications`
  MODIFY `classification_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
