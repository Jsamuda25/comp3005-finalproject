package org.example.model;

import org.example.InputScanner;
import org.example.PostgresConnection;
import org.example.View;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

/**
 * The Admin class extends the User class and represents an admin user in the system.
 * It provides methods for managing roomId bookings, classes, and billing.
 */
public class Admin extends User {

    // Connection to the PostgreSQL database
    private final Connection connection = PostgresConnection.connect();

    /**
     * Constructs an Admin object by calling the super constructor of the User class.
     *
     * @param user the User object to be converted to an Admin object
     */
    public Admin(User user) {
        super(user);
    }

    /**
     * Get the type of fee based on the type_of_fee column in the billing table
     *
     * @param type_of_fee the type of fee
     * @return the type of fee
     */
    private static String getTypeOfFee(int type_of_fee) {
        return switch (type_of_fee) {
            case 0 -> "Membership";
            case 1 -> "Class";
            case 2 -> "Training Session";
            default -> "Unknown";
        };
    }

    /**
     * This method retrieves roomId bookings from the database and returns them as an ArrayList of RoomBooking records.
     *
     * @return an ArrayList of RoomBooking records
     */
    private ArrayList<RoomBooking> viewRoomBookingsHelper() {
        // get roomId bookings from database
        ArrayList<RoomBooking> bookings = new ArrayList<>();

        try {
            String query = "SELECT * FROM RoomBooking ORDER BY date";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                RoomBooking booking = new RoomBooking(
                        resultSet.getInt("room_booking_id"),
                        resultSet.getInt("room_id"),
                        resultSet.getInt("class_id"),
                        resultSet.getDate("date"),
                        resultSet.getTime("start_time"),
                        resultSet.getTime("end_time")
                );
                bookings.add(booking);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving roomId bookings.");
        }

        return bookings;
    }

    /**
     * This method retrieves roomId bookings from the database and prints them.
     */
    public void viewRoomBookings() {
        ArrayList<RoomBooking> bookings = viewRoomBookingsHelper();

        if (bookings.isEmpty()) {
            System.out.println("No roomId bookings found.");
            System.out.println();
            return;
        }

        System.out.println("Room Bookings:");
        for (RoomBooking booking : bookings) {
            System.out.println("Room Booking ID: " + booking.roomBookingId);
            System.out.println("Room ID: " + booking.roomId);
            System.out.println("Room Number:" + getRoomNumber(booking.roomId));
            System.out.println("Class ID: " + booking.classId);
            System.out.println("Date: " + booking.Date);
            System.out.println("Start Time: " + booking.startTime);
            System.out.println("End Time: " + booking.endTime);
            System.out.println();
        }
    }

    /**
     * This method allows the admin to cancel a roomId booking.
     */
    public void cancelRoomBooking() {
        // Print roomId bookings
        ArrayList<RoomBooking> bookings = viewRoomBookingsHelper();
        System.out.println("Select a roomId booking to cancel:");
        for (int i = 0; i < bookings.size(); i++) {
            RoomBooking booking = bookings.get(i);
            System.out.print((i + 1) + ". Room Booking ID: " + booking.roomBookingId);
            System.out.print(", Room Number: " + getRoomNumber(booking.roomId));
            System.out.print(", Date: " + booking.Date);
            System.out.println();
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
            String query = "DELETE FROM RoomBooking WHERE room_booking_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, booking.roomBookingId);
            preparedStatement.executeUpdate();
            System.out.println("Room booking cancelled successfully.");
        } catch (SQLException e) {
            System.err.println("Error cancelling roomId booking.");
            return;
        }
        cancelClass(booking.classId);
    }

    /**
     * This method allows the admin to cancel a class.
     *
     * @param classId the ID of the class to be cancelled
     */
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

    /**
     * This method allows the admin to monitor equipment maintenance.
     */
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

    /**
     * This method allows the admin to manage classes.
     * The admin can create new classes, edit existing classes, and view all classes.
     */
    public void classScheduleUpdating() {
        while (true) {
            System.out.println("1. create new class");
            System.out.println("2. edit existing class roomId");
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

    /**
     * This method is used to create a new class in the system.
     */
    public void createNewClass() {
        try {
            Scanner scanner = InputScanner.getInstance();

            // Get class details from the user
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

            // Check if roomId is available
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
            String roomBookingInsertQuery = "INSERT INTO RoomBooking (room_id, class_id, date, start_time, end_time) " +
                                            "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement roomBookingInsertStatement = connection.prepareStatement(roomBookingInsertQuery);
            roomBookingInsertStatement.setInt(1, roomId);
            roomBookingInsertStatement.setInt(2, classId);
            roomBookingInsertStatement.setDate(3, new Date(startDate.getTime()));
            roomBookingInsertStatement.setTime(4, new Time(startDate.getTime()));
            roomBookingInsertStatement.setTime(5, new Time(endDate.getTime()));
            roomBookingInsertStatement.executeUpdate();

            System.out.println("New class created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating new class: " + e.getMessage());
        }
    }

    /**
     * This method allows the admin to edit the time and date of a class.
     */
    public void editClassTimeDate() {
        try {
            viewClasses();

            Scanner scanner = InputScanner.getInstance();

            System.out.print("Enter the ID of the class to change the time/date for: ");
            int classId = scanner.nextInt();
            scanner.nextLine();

            Timestamp newStartTime;
            Timestamp newEndTime;

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

            // Get new roomId ID
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

            // Check if new start time, end time, and roomId are available
            if (!isTrainerAvailable(trainerId, newStartTime, newEndTime)) {
                System.out.println("The trainer is not available during this time.");
                return;
            }

            if (!isRoomAvailable(newRoomId, newStartTime, newEndTime)) {
                System.out.println("The new roomId is not available during this time.");
                return;
            }

            // Update class time/date and roomId
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

            System.out.println("Class time/date and roomId updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error editing class time/date and roomId: " + e.getMessage());
        }
    }

    /**
     * This method allows the admin to edit the name of a class.
     */
    public void editClassName() {
        try {
            viewClasses();  // Display all classes
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

    /**
     * This method allows the admin to edit the roomId of a class.
     */
    public void editClassRoom() {
        try {
            viewClasses();  // Display all classes
            Scanner scanner = InputScanner.getInstance();

            System.out.print("Enter the ID of the class to change the roomId for: ");
            int classId = scanner.nextInt();

            System.out.print("Enter the new Room ID: ");
            int newRoomId = scanner.nextInt();

            // Get class start and end time
            Timestamp classStartTime;
            Timestamp classEndTime;
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

            // Check if new roomId is available during class time
            if (!isRoomAvailable(newRoomId, classStartTime, classEndTime)) {
                System.out.println("The new roomId is not available during this time.");
                return;
            }

            // Update class roomId
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

            System.out.println("Class roomId updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error editing class roomId: " + e.getMessage());
        }
    }

    /**
     * This method allows the admin to change the trainer of a class.
     * It first displays all the classes, then prompts the admin to enter the ID of the class they want to change the trainer for.
     * The admin then enters the ID of the new trainer.
     * The method checks if the new trainer is available during the class time.
     * If the new trainer is available, the method updates the trainer of the class in the database.
     */
    public void editClassTrainer() {
        try {
            viewClasses();

            Scanner scanner = InputScanner.getInstance();

            System.out.print("Enter the ID of the class to change the trainer for: ");
            int classId = scanner.nextInt();

            System.out.print("Enter the new Trainer ID: ");
            int newTrainerId = scanner.nextInt();

            // Get class start and end time
            Timestamp classStartTime;
            Timestamp classEndTime;
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

    /**
     * This method checks if a trainer is available during a specific time period.
     * It queries the TrainerAvailability table in the database to see if the trainer is available during the specified start and end time.
     *
     * @param trainerId the ID of the trainer
     * @param startDate the start time of the period
     * @param endDate   the end time of the period
     * @return true if the trainer is available, false otherwise
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * This method checks if a roomId is available during a specific time period.
     * It queries the RoomBooking table in the database to see if the roomId is available during the specified start and end time.
     *
     * @param roomId    the ID of the roomId
     * @param startDate the start time of the period
     * @param endDate   the end time of the period
     * @return true if the roomId is available, false otherwise
     * @throws SQLException if a database access error occurs
     */
    private boolean isRoomAvailable(int roomId, Timestamp startDate, Timestamp endDate) throws SQLException {

        // Verify if the room exists
        String roomQuery = "SELECT COUNT(*) AS count FROM Room WHERE room_id = ?";
        PreparedStatement roomStatement = connection.prepareStatement(roomQuery);
        roomStatement.setInt(1, roomId);
        ResultSet roomResultSet = roomStatement.executeQuery();
        roomResultSet.next();
        int roomCount = roomResultSet.getInt("count");
        if (roomCount == 0) {
            System.out.println("Room with ID " + roomId + " does not exist.");
            return false;
        }

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

    /**
     * This method displays all the classes in the system.
     * It queries the Class table in the database and prints out the details of each class.
     */
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
                System.out.println("roomId ID: " + resultSet.getInt("room_id"));
                System.out.println("Room Number: " + getRoomNumber(resultSet.getInt("room_id")));
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving class data.");
        }
    }

    /**
     * This method displays all the billing records that have been paid.
     * It queries the Billing table in the database and prints out the details of each billing record.
     */
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
                System.out.println("Billing ID: " + resultSet.getInt("billing_id"));
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println("Type of Fee" + getTypeOfFee(resultSet.getInt("type_of_fee")));
                System.out.println("Amount: " + resultSet.getDouble("fee"));
                System.out.println("Date: " + resultSet.getDate("date"));
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving billing data.");
        }
    }

    /**
     * This method processes payments for unpaid bills.
     * It first displays all the unpaid bills, then prompts the admin to enter the ID of the bill they want to process payment for.
     * The method then updates the paid status of the bill in the database.
     */
    public void processRefunds() {
        // Print out all Payments that are not paid
        System.out.println("Payments to Process Refunds: ");
        try {
            String query = "SELECT billing_id, name, fee, type_of_fee, date FROM billing " +
                           "JOIN public.users u on u.id = billing.member_id " +
                           "WHERE paid = false " +
                           "ORDER BY date DESC";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String typeOfFee = getTypeOfFee(resultSet.getInt("type_of_fee"));
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
        System.out.print("Enter the billing ID to process refund payment or -1 to go Back: ");
        Optional<Integer> billingId = View.getIntegerInput();
        while (billingId.isEmpty()) {
            System.out.println("Invalid input. Please enter a number.");
            billingId = View.getIntegerInput();
        }
        if (billingId.get() == -1) return;

        // Process Refund
        try {
            String query = "DELETE FROM billing WHERE billing_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, billingId.get());
            preparedStatement.executeUpdate();
            System.out.println("Refund Processed.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error processing refund.");
        }

    }

    /**
     * The RoomBooking record represents a roomId booking in the system.
     */
    private record RoomBooking(int roomBookingId, int roomId, int classId, Date Date,
                               Time startTime, Time endTime) {
    }

    /**
     * The Session record represents a session in the system.
     */
    private record Session(int trainer_id, Timestamp startTime, Timestamp endTime) {
    }
}
