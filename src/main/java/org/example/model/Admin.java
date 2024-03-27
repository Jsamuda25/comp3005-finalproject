package org.example.model;

import org.example.Main;
import org.example.View;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class Admin extends User {

    private final Connection connection = Main.connection;

    public Admin(User user) {
        super(user);
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
        try {
            String query = "SELECT trainer_id, start_date, end_date FROM trainingsessions WHERE session_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, session_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                trainingSession = new Session(resultSet.getInt("trainer_id"), resultSet.getTimestamp("start_date"), resultSet.getTimestamp("end_date"));
            }
        } catch (SQLException e) {
            System.out.println("Error getting training session details.");
            return;
        }
        assert trainingSession != null;

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

    private static String getTypeOfFee(ResultSet resultSet) throws SQLException {
        return switch (resultSet.getInt("type_of_fee")) {
            case 0 -> "Membership";
            case 1 -> "Class";
            case 2 -> "Training Session";
            default -> "Unknown";
        };
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
