-- Sample data for Users Table
INSERT INTO Users (username, password, name, typeOfUser) VALUES
('john_doe', 'password123', 'John Doe', 'MEMBER'),
('trainer_jane', 'trainerpass', 'Jane Smith', 'TRAINER'),
('admin_jack', 'adminpass', 'Jack Rowe', 'ADMIN');

-- Sample data for TrainerAvailability Table
INSERT INTO TrainerAvailability (trainer_id, start_time, end_time) VALUES
(2, '2023-02-25 09:00:00', '2023-02-25 11:00:00'),
(2, '2024-01-16 10:00:00', '2024-01-16 12:00:00'),
(2, '2024-01-22 09:00:00', '2024-01-22 11:00:00'),
(2, '2024-04-25 09:00:00', '2024-04-25 11:00:00'),
(2, '2024-04-26 10:00:00', '2024-04-26 12:00:00'),
(2, '2024-04-27 09:00:00', '2024-04-27 11:00:00'),
(2, '2024-04-28 10:00:00', '2024-04-28 12:00:00'),
(2, '2024-05-09 09:00:00', '2024-05-09 11:00:00'),
(2, '2024-05-14 10:00:00', '2024-05-14 12:00:00'),
(2, '2024-06-11 09:00:00', '2024-06-11 11:00:00'),
(2, '2024-06-14 10:00:00', '2024-06-14 12:00:00'),
(2, '2024-06-15 10:00:00', '2024-06-15 12:00:00');

-- Sample data for FitnessGoal Table
INSERT INTO FitnessGoal (userId, title, value, endDate, completed) VALUES
(1, 'Weight Loss', 'Lose 5kg', '2024-06-30', FALSE),
(1, 'Muscle Gain', 'Gain 2kg of muscle', '2024-06-30', TRUE);

-- Sample data for HealthMetrics Table
INSERT INTO HealthMetrics (Member_ID, Metric_Type, Value, Date_Recorded, Unit, Notes) VALUES
(1, 'Weight', 70.5, '2024-03-20', 'kg', 'Measured in the morning'),
(1, 'Body Fat Percentage', 20.0, '2024-03-20', '%', 'Measured using calipers');

-- Sample data for TrainingSessions Table
INSERT INTO TrainingSessions (member_id, trainer_id, start_date, end_date, cancelled) VALUES
(1, 2, '2024-03-25 09:00:00', '2024-03-25 10:00:00', false),
(1, 2, '2024-03-26 10:00:00', '2024-03-26 11:00:00',false);

-- Sample data for Room Table
INSERT INTO Room(room_number) VALUES
(20),
(233);

-- Sample data for Class Table
INSERT INTO Class (trainer_id, class_name, start_date, end_date, room_id) VALUES
(2, 'Yoga Class', '2024-04-01 09:00:00', '2024-04-01 10:00:00', 1),
(2, 'CrossFit Class', '2024-04-02 10:00:00', '2024-04-02 11:00:00', 2),
(2, 'Boxing Class', '2024-04-14 10:00:00', '2024-04-14 11:00:00', 2),
(2, 'Cycling Class', '2024-05-11 09:00:00', '2024-05-11 10:00:00', 1),
(2, 'Pilates Class', '2024-05-14 10:00:00', '2024-05-14 11:00:00', 2);

-- Sample data for ClassMembers/Takes Table
INSERT INTO ClassMembers (class_id, member_id) VALUES
(1, 1),
(2, 1);

-- Sample data for RoomBooking Table
INSERT INTO RoomBooking (room_id, class_id, date, start_time, end_time) VALUES
(1, 1, '2024-04-01', '09:00:00', '10:00:00'),
(2, 2, '2024-04-02', '10:00:00', '11:00:00'),
(2, 3, '2024-04-14', '10:00:00', '11:00:00'),
(1, 4, '2024-05-11', '09:00:00', '10:00:00'),
(2, 5, '2024-05-14', '10:00:00', '11:00:00');

-- Type of fee, 0 is Membership, 1 is Class, 2 is Training Session
-- Sample data for Billing Table
INSERT INTO Billing (member_id, fee, type_of_fee, paid, date) VALUES
(1, 150.00, 0, true, '2024-03-25 9:00:00'),
(1, 50.00, 1, true, '2024-03-25 10:00:00'),
(1, 60.00, 2, false, '2024-04-01 10:00:00');

-- Sample data for Maintenance Table
INSERT INTO Maintenance (equipment_name, last_maintained) VALUES
('Bench Press', '2024-03-22'),
('Treadmill', '2024-03-19'),
('Dumbbells', '2024-03-20');

-- Sample data for ExerciseRoutines Table
INSERT INTO exerciseroutines (name, instruction) VALUES 
('Push-ups', '1. Start in a plank position with your hands slightly wider than shoulder-width apart;2. Lower your body until your chest nearly touches the floor;3. Push yourself back up to the starting position;'),
('Squats', '1. Stand with your feet shoulder-width apart;2. Lower your body by bending your knees and pushing your hips back;3. Keep your chest up and your back straight;4. Push through your heels to return to the starting position;'),
('Plank', '1. Begin in a push-up position with your arms straight;2. Hold your body in a straight line from head to heels, engaging your core muscles;3. Hold the position for the desired duration, keeping your back flat and your abs tight;'),
('Lunges', '1. Stand tall with your feet hip-width apart;2. Take a big step forward with one leg and lower your body until both knees are bent at a 90-degree angle;3. Keep your front knee over your ankle and your back knee just above the floor;4. Push back up to the starting position and repeat with the other leg;'),
('Bicycle Crunches', '1. Lie flat on your back with your hands behind your head;2. Lift your legs off the ground and bend them at the knees;3. Bring your right elbow towards your left knee while straightening your right leg;4. Switch sides, bringing your left elbow towards your right knee while straightening your left leg;5. Continue alternating sides in a pedalling motion;')
;