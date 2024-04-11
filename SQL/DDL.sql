-- Define UserType ENUM
CREATE TYPE UserType AS ENUM ('MEMBER', 'TRAINER', 'ADMIN');

-- Users Table
CREATE TABLE Users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    name VARCHAR(255),
    typeOfUser UserType
);

-- TrainerAvailability Table
CREATE TABLE TrainerAvailability (
    trainer_avail_id SERIAL PRIMARY KEY,
    trainer_id INT REFERENCES Users(id),
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

-- FitnessGoal Table
CREATE TABLE FitnessGoal (
    goalId SERIAL PRIMARY KEY,
    userId INT REFERENCES Users(id),
    title VARCHAR(255),
    value VARCHAR(255),
    endDate DATE,
    completed BOOLEAN
);

-- HealthMetrics Table
CREATE TABLE HealthMetrics (
    Metric_ID SERIAL PRIMARY KEY,
    Member_ID INT REFERENCES Users(id),
    Metric_Type VARCHAR(255),
    Value FLOAT,
    Date_Recorded DATE,
    Unit VARCHAR(50),
    Notes TEXT
);

-- TrainingSessions Table
CREATE TABLE TrainingSessions (
    session_id SERIAL PRIMARY KEY,
    member_id INT REFERENCES Users(id),
    trainer_id INT REFERENCES Users(id),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    cancelled BOOLEAN
);

-- Room Table
CREATE Table Room (
    room_id SERIAL PRIMARY KEY,
    room_number INT UNIQUE NOT NULL
);

-- Class Table
CREATE TABLE Class (
    class_id SERIAL PRIMARY KEY,
    trainer_id INT REFERENCES Users(id),
    class_name VARCHAR(255),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    room_id INT REFERENCES Room(room_id)
);

-- ClassMembers/Takes Table
CREATE TABLE ClassMembers (
    student_id SERIAL PRIMARY KEY,
    class_id INT REFERENCES Class(class_id),
    member_id INT REFERENCES Users(id)
);

-- RoomBooking Table
CREATE TABLE RoomBooking (
    room_booking_id SERIAL PRIMARY KEY,
    room_id INT REFERENCES Room(room_id),
    class_id INT UNIQUE REFERENCES Class(class_id),
    date DATE,
    start_time TIME,
    end_time TIME
);

-- Billing Table
CREATE TABLE Billing (
    billing_id SERIAL PRIMARY KEY,
    member_id INT REFERENCES Users(id),
    fee FLOAT,
    type_of_fee INT,
    paid BOOLEAN,
    date TIMESTAMP
);

-- Maintenance Table
CREATE TABLE Maintenance (
     equipment_id SERIAL PRIMARY KEY,
     equipment_name VARCHAR(100) NOT NULL,
     last_maintained DATE
);

-- ExerciseRoutines Table
CREATE TABLE ExerciseRoutines (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    instruction TEXT
);
    