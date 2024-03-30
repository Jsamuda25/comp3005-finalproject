package org.example.model;

import org.example.InputScanner;
import org.example.Main;
import org.example.View;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class Admin extends User {

    private final Connection connection = Main.connection;

    public Admin(User user) {
        super(user);
    }

    private static String getTypeOfFee(ResultSet resultSet) throws SQLException {
        return switch (resultSet.getInt("type_of_fee")) {
            case 0 -> "Membership";
            case 1 -> "Class";
            case 2 -> "Training Session";
            default -> "Unknown";
        };
    }

    /**
     * Admin can view room bookings from the database
     */
    private ArrayList<RoomBooking> viewRoomBookingsHelper() {
        // get room bookings from database
        ArrayList<RoomBooking> bookings = new ArrayList<>();

        try {
            String query = "SELECT * FROM RoomBooking ORDER BY date";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                RoomBooking booking = new RoomBooking(resultSet.getInt("room_id"), resultSet.getInt("typeofbooking"), resultSet.getInt("session_id"), resultSet.getInt("class_id"), resultSet.getDate("date"), resultSet.getTime("start_time"), resultSet.getTime("end_time"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving room bookings.");
        }

        return bookings;
    }

    public void viewRoomBookings() {
        ArrayList<RoomBooking> bookings = viewRoomBookingsHelper();

        if (bookings.isEmpty()) {
            System.out.println("No room bookings found.");
            System.out.println();
            return;
        }

        System.out.println("Room Bookings:");
        for (RoomBooking booking : bookings) {
            System.out.println("Room Booking ID: " + booking.roomBookingId);
            System.out.println("Type of Booking: " + booking.typeOfBooking);
            System.out.println("Session ID: " + booking.sessionId);
            System.out.println("Class ID: " + booking.class_id);
            System.out.println("Date: " + booking.Date);
            System.out.println("Start Time: " + booking.startTime);
            System.out.println("End Time: " + booking.endTime);
            System.out.println();
        }
    }

    public void cancelRoomBooking() {
        // Print room bookings
        ArrayList<RoomBooking> bookings = viewRoomBookingsHelper();
        System.out.println("Select a room booking to cancel:");
        for (int i = 0; i < bookings.size(); i++) {
            RoomBooking booking = bookings.get(i);
            System.out.print((i + 1) + ". Room Booking ID: " + booking.roomBookingId);
            System.out.println("; Type of Booking: " + (booking.typeOfBooking == 0 ? "Training Session" : "Class"));
        }
        System.out.println((bookings.size() + 1) + ". Back");

        // Get user choice
        System.out.print("Enter your choice: ");
        Optional<Integer> choice = View.getIntegerInput();
        while (choice.isEmpty() || choice.get() < 1 || choice.get() > bookings.size() + 1) {
            System.out.println("Invalid choice. Please enter a number between 1 and " + bookings.size() + ".");
            choice = View.getIntegerInput();
        }
        if (choice.get() == bookings.size() + 1) return;
        RoomBooking booking = bookings.get(choice.get() - 1);

        // Cancel Room Booking
        try {
            String query = "DELETE FROM RoomBooking WHERE room_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, booking.roomBookingId);
            preparedStatement.executeUpdate();
            System.out.println("Room booking cancelled successfully.");
        } catch (SQLException e) {
            System.err.println("Error cancelling room booking.");
            return;
        }

        // Cancel Training Session or Class
        if (booking.typeOfBooking == 0) {
            cancelTrainingSession(booking.sessionId);
        } else {
            cancelClass(booking.class_id);
        }
    }

    private void cancelClass(int classId) {
        // Cancel Class and update the trainer's availability

        // Get the Session Info
        Session classSession = null;
        try {
            String query = "SELECT trainer_id, start_date, end_date FROM class WHERE class_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, classId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                classSession = new Session(resultSet.getInt("trainer_id"), resultSet.getTimestamp("start_date"), resultSet.getTimestamp("end_date"));
            }
        } catch (SQLException e) {
            System.out.println("Error Getting Details from Class session.");
            return;
        }
        assert classSession != null;

        // Get the class members
        ArrayList<Integer> classMembers = new ArrayList<>();
        try {
            String query = "SELECT member_id FROM classmembers WHERE class_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, classId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                classMembers.add(resultSet.getInt("member_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving class members.");
            return;
        }

        // Cancel the class members' billing if its unpaid
        for (int memberId : classMembers) {
            try {
                String query = "SELECT billing_id FROM billing WHERE member_id = ? AND type_of_fee = 1 AND paid = false";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, memberId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int billingId = resultSet.getInt("billing_id");
                    query = "DELETE FROM billing WHERE billing_id = ?";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, billingId);
                    preparedStatement.executeUpdate();
                    System.out.println("Billing " + billingId + ": Member " + memberId + " bill for the Class Session is cancelled.");
                } else {
                    // remove bill from billing table and print "Refund Processed for member_id with billing_id"
                    query = "DELETE FROM billing WHERE member_id = ? AND type_of_fee = 1 AND paid = true";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, memberId);
                    preparedStatement.executeUpdate();
                    System.out.println("Class Session Refund Processed for member_id " + memberId);
                }
            } catch (SQLException e) {
                System.err.println("Error cancelling class members' billing.");
                return;
            }
        }

        // Delete the class members
        try {
            String query = "DELETE FROM classmembers WHERE class_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, classId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing class members.");
            return;
        }

        // Delete the class
        try {
            String query = "DELETE FROM class WHERE class_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, classId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error cancelling class.");
            return;
        }

        // Insert a new row into the TrainerAvailability table
        try {
            String query = "INSERT INTO TrainerAvailability (trainer_id, start_time, end_time) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, classSession.trainer_id);
            preparedStatement.setTimestamp(2, classSession.startTime);
            preparedStatement.setTimestamp(3, classSession.endTime);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error Updating Trainer Availability.");
        }
    }

    private void cancelTrainingSession(int session_id) {
        // Cancel Training Session and update the trainer's availability

        // Get the trainer's ID
        Session trainingSession = null;
        int member_id = 0;
        try {
            String query = "SELECT trainer_id, member_id, start_date, end_date FROM trainingsessions WHERE session_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, session_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                trainingSession = new Session(resultSet.getInt("trainer_id"), resultSet.getTimestamp("start_date"), resultSet.getTimestamp("end_date"));
                member_id = resultSet.getInt("member_id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting training session details.");
            return;
        }
        assert trainingSession != null;

        // Cancel the member's billing if its unpaid
        try {
            String query = "SELECT billing_id FROM billing WHERE member_id = ? AND type_of_fee = 2 AND paid = false";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, member_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int billingId = resultSet.getInt("billing_id");
                query = "DELETE FROM billing WHERE billing_id = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, billingId);
                preparedStatement.executeUpdate();
                System.out.println("Billing " + billingId + ": Member " + member_id + " bill for the Training Session is cancelled.");
            } else {
                // remove bill from billing table and print "Refund Processed for member_id with billing_id"
                query = "DELETE FROM billing WHERE member_id = ? AND type_of_fee = 1 AND paid = true";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, member_id);
                preparedStatement.executeUpdate();
                System.out.println("Training Session Refund Processed for member_id " + member_id);
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling member's billing.");
            return;
        }

        // Delete the training session
        try {
            String query = "DELETE FROM trainingsessions WHERE session_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, session_id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error cancelling training session.");
            return;
        }

        // Insert a new row into the TrainerAvailability table
        try {
            String query = "INSERT INTO TrainerAvailability (trainer_id, start_time, end_time) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, trainingSession.trainer_id);
            preparedStatement.setTimestamp(2, trainingSession.startTime);
            preparedStatement.setTimestamp(3, trainingSession.endTime);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating Trainer Availability.");
        }
    }

    public void equipmentMaintenanceMonitoring() {
        // Print out all equipment and let user update the maintanence data for any equipment to today's date
        try {
            String query = "SELECT * FROM maintenance ORDER BY last_maintained DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Equipment ID: " + resultSet.getInt("equipment_id"));
                System.out.println("Equipment Name: " + resultSet.getString("equipment_name"));
                System.out.println("Last Date Maintained: " + resultSet.getDate("last_maintained"));
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment data.");
            return;
        }

        // Show two options to update maintenance date or go back
        System.out.println("1. Update maintenance date");
        System.out.println("2. Back");
        System.out.print("Enter your choice: ");
        Optional<Integer> choice = View.getIntegerInput();
        while (choice.isEmpty() || choice.get() < 1 || choice.get() > 2) {
            System.out.println("Invalid choice. Please enter a number between 1 and 2.");
            choice = View.getIntegerInput();
        }
        if (choice.get() == 2) return;

        // Get equipment ID from user
        System.out.print("Enter the equipment ID to update maintenance date or -1 to go Back: ");
        Optional<Integer> equipmentId = View.getIntegerInput();
        while (equipmentId.isEmpty()) {
            System.out.println("Invalid input. Please enter a number.");
            equipmentId = View.getIntegerInput();
        }

        if (equipmentId.get() == -1) return;

        // Update maintenance date
        try {
            String query = "UPDATE maintenance SET last_maintained = ? WHERE equipment_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
            preparedStatement.setInt(2, equipmentId.get());
            preparedStatement.executeUpdate();
            System.out.println("Maintenance date updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating maintenance date.");
        }
    }

    public void classScheduleUpdating() {
        while (true) {
            System.out.println("1. create new class");
            System.out.println("2. edit existing class room");
            System.out.println("3. edit existing class time/date");
            System.out.println("4. edit existing class trainer");
            System.out.println("5. edit existing class name");
            System.out.println("6. view classes");
            System.out.println("7. exit");
            Optional<Integer> choice = View.getIntegerInput();
            while (choice.isEmpty() || choice.get() < 1 || choice.get() > 7) {
                System.out.println("Invalid choice. Please enter a number between 1 and 7.");
                choice = View.getIntegerInput();
            }
            switch (choice.get()) {
                case 1:
                    createNewClass();
                    break;
                case 2:
                    editClassRoom();
                    break;
                case 3:
                    editClassTimeDate();
                    break;
                case 4:
                    editClassTrainer();
                    break;
                case 5:
                    editClassName();
                    break;
                case 6:
                    viewClasses();
                case 7:
                    System.out.println("exiting");
                    return;
            }

        }

    }

    public void createNewClass() {
        try {
            Scanner scanner = InputScanner.getInstance();

            System.out.println("Enter class details:");
            System.out.print("Trainer ID: ");
            int trainerId = scanner.nextInt();
            scanner.nextLine();
            System.out.print("Class Name: ");
            String className = scanner.nextLine();
            System.out.print("Start Date (YYYY-MM-DD HH:MM:SS): ");
            String startDateStr = scanner.nextLine();
            Timestamp startDate = Timestamp.valueOf(startDateStr);
            System.out.print("End Date (YYYY-MM-DD HH:MM:SS): ");
            String endDateStr = scanner.nextLine();
            Timestamp endDate = Timestamp.valueOf(endDateStr);
            System.out.print("Room ID: ");
            int roomId = scanner.nextInt();

            // Check if trainer is available
            if (!isTrainerAvailable(trainerId, startDate, endDate)) {
                System.out.println("Trainer is not available during this time.");
                return;
            }

            // Check if room is available
            if (!isRoomAvailable(roomId, startDate, endDate)) {
                System.out.println("Room is not available during this time.");
                return;
            }

            // Insert new class into the database
            String classInsertQuery = "INSERT INTO Class (trainer_id, class_name, start_date, end_date, room_id) " +
                                      "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement classInsertStatement = connection.prepareStatement(classInsertQuery, Statement.RETURN_GENERATED_KEYS);
            classInsertStatement.setInt(1, trainerId);
            classInsertStatement.setString(2, className);
            classInsertStatement.setTimestamp(3, startDate);
            classInsertStatement.setTimestamp(4, endDate);
            classInsertStatement.setInt(5, roomId);
            classInsertStatement.executeUpdate();

            // Get the generated class ID
            ResultSet generatedKeys = classInsertStatement.getGeneratedKeys();
            int classId;
            if (generatedKeys.next()) {
                classId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to create class, no ID obtained.");
            }

            // Insert entry into RoomBooking
            String roomBookingInsertQuery = "INSERT INTO RoomBooking (typeOfBooking, session_id, class_id, date, start_time, end_time) " +
                                            "VALUES (1, NULL, ?, ?, ?, ?)";
            PreparedStatement roomBookingInsertStatement = connection.prepareStatement(roomBookingInsertQuery);
            roomBookingInsertStatement.setInt(1, classId);
            roomBookingInsertStatement.setDate(2, new Date(startDate.getTime()));
            roomBookingInsertStatement.setTime(3, new Time(startDate.getTime()));
            roomBookingInsertStatement.setTime(4, new Time(endDate.getTime()));
            roomBookingInsertStatement.executeUpdate();

            System.out.println("New class created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating new class: " + e.getMessage());
        }
    }

    public void editClassTimeDate() {
        try {
            viewClasses();

            Scanner scanner = InputScanner.getInstance();

            System.out.print("Enter the ID of the class to change the time/date for: ");
            int classId = scanner.nextInt();
            scanner.nextLine();

            Timestamp newStartTime = null;
            Timestamp newEndTime = null;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setLenient(false);

            System.out.println("Enter new start time (format: YYYY-MM-DD HH:MM:SS):");
            String startInput = scanner.nextLine().trim();

            System.out.println("Enter new end time (format: YYYY-MM-DD HH:MM:SS):");
            String endInput = scanner.nextLine().trim();

            try {
                newStartTime = new Timestamp(dateFormat.parse(startInput).getTime());
                newEndTime = new Timestamp(dateFormat.parse(endInput).getTime());
            } catch (ParseException e) {
                System.out.println("Invalid timestamp format.");
                return;
            }

            // Get new room ID
            System.out.print("Enter the new Room ID: ");
            int newRoomId = scanner.nextInt();

            // Get class trainer ID
            int trainerId;
            String getTrainerIdQuery = "SELECT trainer_id FROM Class WHERE class_id = ?";
            PreparedStatement getTrainerIdStatement = connection.prepareStatement(getTrainerIdQuery);
            getTrainerIdStatement.setInt(1, classId);
            ResultSet trainerIdResultSet = getTrainerIdStatement.executeQuery();
            if (trainerIdResultSet.next()) {
                trainerId = trainerIdResultSet.getInt("trainer_id");
            } else {
                System.out.println("Class with ID " + classId + " not found.");
                return;
            }

            // Check if new start time, end time, and room are available
            if (!isTrainerAvailable(trainerId, newStartTime, newEndTime)) {
                System.out.println("The trainer is not available during this time.");
                return;
            }

            if (!isRoomAvailable(newRoomId, newStartTime, newEndTime)) {
                System.out.println("The new room is not available during this time.");
                return;
            }

            // Update class time/date and room
            String updateClassTimeRoomQuery = "UPDATE Class SET start_date = ?, end_date = ?, room_id = ? WHERE class_id = ?";
            PreparedStatement updateClassTimeRoomStatement = connection.prepareStatement(updateClassTimeRoomQuery);
            updateClassTimeRoomStatement.setTimestamp(1, newStartTime);
            updateClassTimeRoomStatement.setTimestamp(2, newEndTime);
            updateClassTimeRoomStatement.setInt(3, newRoomId);
            updateClassTimeRoomStatement.setInt(4, classId);
            updateClassTimeRoomStatement.executeUpdate();

            // Update RoomBooking table
            String updateRoomBookingQuery = "UPDATE RoomBooking SET date = ?, start_time = ?, end_time = ?, room_id = ? WHERE class_id = ?";
            PreparedStatement updateRoomBookingStatement = connection.prepareStatement(updateRoomBookingQuery);
            updateRoomBookingStatement.setDate(1, new Date(newStartTime.getTime()));
            updateRoomBookingStatement.setTime(2, new Time(newStartTime.getTime()));
            updateRoomBookingStatement.setTime(3, new Time(newEndTime.getTime()));
            updateRoomBookingStatement.setInt(4, newRoomId);
            updateRoomBookingStatement.setInt(5, classId);
            updateRoomBookingStatement.executeUpdate();

            System.out.println("Class time/date and room updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error editing class time/date and room: " + e.getMessage());
        }
    }

    public void editClassName() {
        try {
            viewClasses();

            Scanner scanner = InputScanner.getInstance();

            // Get class ID to edit name
            System.out.print("Enter the ID of the class to change the name for: ");
            int classId = scanner.nextInt();
            scanner.nextLine();

            // Get new class name
            System.out.print("Enter the new class name: ");
            String newClassName = scanner.nextLine();

            // Update class name in the database
            String updateClassNameQuery = "UPDATE Class SET class_name = ? WHERE class_id = ?";
            PreparedStatement updateClassNameStatement = connection.prepareStatement(updateClassNameQuery);
            updateClassNameStatement.setString(1, newClassName);
            updateClassNameStatement.setInt(2, classId);
            updateClassNameStatement.executeUpdate();

            System.out.println("Class name updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error editing class name: " + e.getMessage());
        }
    }

    public void editClassRoom() {
        try {
            viewClasses();

            Scanner scanner = InputScanner.getInstance();

            System.out.print("Enter the ID of the class to change the room for: ");
            int classId = scanner.nextInt();

            System.out.print("Enter the new Room ID: ");
            int newRoomId = scanner.nextInt();

            // Get class start and end time
            Timestamp classStartTime = null;
            Timestamp classEndTime = null;
            String getClassTimeQuery = "SELECT start_date, end_date FROM Class WHERE class_id = ?";
            PreparedStatement getClassTimeStatement = connection.prepareStatement(getClassTimeQuery);
            getClassTimeStatement.setInt(1, classId);
            ResultSet classTimeResultSet = getClassTimeStatement.executeQuery();
            if (classTimeResultSet.next()) {
                classStartTime = classTimeResultSet.getTimestamp("start_date");
                classEndTime = classTimeResultSet.getTimestamp("end_date");
            } else {
                System.out.println("Class with ID " + classId + " not found.");
                return;
            }

            // Check if new room is available during class time
            if (!isRoomAvailable(newRoomId, classStartTime, classEndTime)) {
                System.out.println("The new room is not available during this time.");
                return;
            }

            // Update class room
            String updateClassRoomQuery = "UPDATE Class SET room_id = ? WHERE class_id = ?";
            PreparedStatement updateClassRoomStatement = connection.prepareStatement(updateClassRoomQuery);
            updateClassRoomStatement.setInt(1, newRoomId);
            updateClassRoomStatement.setInt(2, classId);
            updateClassRoomStatement.executeUpdate();

            // Update RoomBooking table
            String updateRoomBookingQuery = "UPDATE RoomBooking SET room_id = ? WHERE class_id = ?";
            PreparedStatement updateRoomBookingStatement = connection.prepareStatement(updateRoomBookingQuery);
            updateRoomBookingStatement.setInt(1, newRoomId);
            updateRoomBookingStatement.setInt(2, classId);
            updateRoomBookingStatement.executeUpdate();

            System.out.println("Class room updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error editing class room: " + e.getMessage());
        }
    }

    public void editClassTrainer() {
        try {
            viewClasses();

            Scanner scanner = InputScanner.getInstance();

            System.out.print("Enter the ID of the class to change the trainer for: ");
            int classId = scanner.nextInt();

            System.out.print("Enter the new Trainer ID: ");
            int newTrainerId = scanner.nextInt();

            // Get class start and end time
            Timestamp classStartTime = null;
            Timestamp classEndTime = null;
            String getClassTimeQuery = "SELECT start_date, end_date FROM Class WHERE class_id = ?";
            PreparedStatement getClassTimeStatement = connection.prepareStatement(getClassTimeQuery);
            getClassTimeStatement.setInt(1, classId);
            ResultSet classTimeResultSet = getClassTimeStatement.executeQuery();
            if (classTimeResultSet.next()) {
                classStartTime = classTimeResultSet.getTimestamp("start_date");
                classEndTime = classTimeResultSet.getTimestamp("end_date");
            } else {
                System.out.println("Class with ID " + classId + " not found.");
                return;
            }

            // Check if new trainer is available during class time
            if (!isTrainerAvailable(newTrainerId, classStartTime, classEndTime)) {
                System.out.println("The new trainer is not available during this time.");
                return;
            }

            // Update class trainer
            String updateClassTrainerQuery = "UPDATE Class SET trainer_id = ? WHERE class_id = ?";
            PreparedStatement updateClassTrainerStatement = connection.prepareStatement(updateClassTrainerQuery);
            updateClassTrainerStatement.setInt(1, newTrainerId);
            updateClassTrainerStatement.setInt(2, classId);
            updateClassTrainerStatement.executeUpdate();

            System.out.println("Class trainer updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error editing class trainer: " + e.getMessage());
        }
    }

    private boolean isTrainerAvailable(int trainerId, Timestamp startDate, Timestamp endDate) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM TrainerAvailability " +
                       "WHERE trainer_id = ? AND ? <= end_time AND start_time <= ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, trainerId);
        preparedStatement.setTimestamp(2, endDate);
        preparedStatement.setTimestamp(3, startDate);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt("count");
        return count > 0;
    }

    private boolean isRoomAvailable(int roomId, Timestamp startDate, Timestamp endDate) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM RoomBooking " +
                       "WHERE room_id = ? AND date = ? " +
                       "AND ? < end_time AND start_time < ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, roomId);
        preparedStatement.setDate(2, new Date(startDate.getTime()));
        preparedStatement.setTime(3, new Time(endDate.getTime()));
        preparedStatement.setTime(4, new Time(startDate.getTime()));
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt("count");
        return count == 0;
    }

    private void viewClasses() {
        try {
            String query = "SELECT * FROM class ORDER BY start_date";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Scheduled classes:");
                System.out.println("Class ID: " + resultSet.getInt("class_id"));
                System.out.println("Class Name: " + resultSet.getString("class_name"));
                System.out.println("Trainer ID: " + resultSet.getInt("trainer_id"));
                System.out.println("Start Date: " + resultSet.getDate("start_date"));
                System.out.println("End Date: " + resultSet.getDate("end_date"));
                System.out.println("room ID: " + resultSet.getInt("room_id"));
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving class data.");
            return;
        }
    }

    public void viewBillingAndPayment() {
        // Print out all billing that are paid
        System.out.println("Billing History: ");

        try {
            String query = "SELECT billing_id, name, fee, type_of_fee, date FROM billing " +
                           "JOIN public.users u on u.id = billing.member_id " +
                           "WHERE paid = true " +
                           "ORDER BY date";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String typeOfFee = getTypeOfFee(resultSet);

                System.out.println("Billing ID: " + resultSet.getInt("billing_id"));
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println("Amount: " + resultSet.getDouble("fee"));
                System.out.println("Type of Fee: " + typeOfFee);
                System.out.println("Date: " + resultSet.getDate("date"));
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving billing data.");
        }

    }

    public void processPayment() {
        // Print out all Payments that are not paid
        System.out.println("Payments to Process: ");
        try {
            String query = "SELECT billing_id, name, fee, type_of_fee, date FROM billing " +
                           "JOIN public.users u on u.id = billing.member_id " +
                           "WHERE paid = false " +
                           "ORDER BY date DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String typeOfFee = getTypeOfFee(resultSet);

                System.out.println("Billing ID: " + resultSet.getInt("billing_id"));
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println("Amount: " + resultSet.getDouble("fee"));
                System.out.println("Type of Fee: " + typeOfFee);
                System.out.println("Date: " + resultSet.getDate("date"));
                System.out.println("Payment Status: Not Paid");
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving bills.");
        }

        // Get user choice
        System.out.print("Enter the billing ID to process payment or -1 to go Back: ");
        Optional<Integer> billingId = View.getIntegerInput();
        while (billingId.isEmpty()) {
            System.out.println("Invalid input. Please enter a number.");
            billingId = View.getIntegerInput();
        }

        if (billingId.get() == -1) return;

        // Process Payment
        try {
            String query = "UPDATE billing SET paid = true WHERE billing_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, billingId.get());
            preparedStatement.executeUpdate();
            System.out.println("Payment processed successfully.");
        } catch (SQLException e) {
            System.err.println("Error processing payment. (Wrong Billing ID)");
        }
    }

    private record RoomBooking(int roomBookingId, int typeOfBooking, int sessionId, int class_id, Date Date,
                               Time startTime, Time endTime) {
    }

    private record Session(int trainer_id, Timestamp startTime, Timestamp endTime) {
    }
}
