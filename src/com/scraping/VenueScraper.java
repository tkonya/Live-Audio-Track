package com.scraping;

import com.models.Venue;
import com.mysql.fabric.xmlrpc.base.Data;
import com.utilities.DatabaseHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Trevor on 5/5/2016.
 */
public abstract class VenueScraper {

    protected static DatabaseHandler databaseHandler;

    protected static void sleepBetween(int minSeconds, int maxSeconds) {
        Random random = new Random();
        int sleepMillis = random.nextInt(((maxSeconds * 1000) - (minSeconds * 1000)) + minSeconds * 1000);
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static void saveVenueURLs(Set<String> venueURLs) {

        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler();
        }

        Set<String> knownURLs = (HashSet<String>) databaseHandler.getCollection(new HashSet<String>(), "SELECT DISTINCT source FROM venues");

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseHandler.getConnection().prepareStatement("INSERT INTO venues (source) VALUES (?)");
            for (String url : venueURLs) {
                if (!knownURLs.contains(url)) {
                    preparedStatement.setString(1, url);
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    protected static void saveVenue(Venue venue) {

        if (databaseHandler == null) {
            databaseHandler = new DatabaseHandler();
        }

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseHandler.getConnection().prepareStatement(
                    "INSERT INTO venues (venue_name, street1, street2, city, state, zip, phone, email, website, source, genre, capacity, min_age, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE venue_name = venue_name"
            );

            preparedStatement.setString(1, venue.getName());
            preparedStatement.setString(2, venue.getStreet1());
            preparedStatement.setString(3, venue.getStreet2());
            preparedStatement.setString(4, venue.getCity());
            preparedStatement.setString(5, venue.getState());
            preparedStatement.setString(6, venue.getZip());
            preparedStatement.setString(7, venue.getPhone());
            preparedStatement.setString(8, venue.getEmail());
            preparedStatement.setString(9, venue.getWebsite());
            preparedStatement.setString(10, venue.getSource());
            preparedStatement.setString(11, venue.getGenre());
            if (venue.getCapacity() != null) {
                preparedStatement.setInt(12, venue.getCapacity());
            } else {
                preparedStatement.setNull(12, Types.INTEGER);
            }
            if (venue.getMinAge() != null) {
                preparedStatement.setInt(13, venue.getMinAge());
            } else {
                preparedStatement.setNull(13, Types.INTEGER);
            }
            preparedStatement.setString(14, venue.getNotes());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void saveVenues(List<Venue> venues) {

        DatabaseHandler databaseHandler = new DatabaseHandler();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = databaseHandler.getConnection().prepareStatement(
                    "INSERT INTO venues (venue_name, street1, street2, city, state, zip, phone, email, website, source, genre, capacity, min_age, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE venue_name = venue_name"
            );

            int batchSize = 0;
            for (Venue venue : venues) {
                preparedStatement.setString(1, venue.getName());
                preparedStatement.setString(2, venue.getStreet1());
                preparedStatement.setString(3, venue.getStreet2());
                preparedStatement.setString(4, venue.getCity());
                preparedStatement.setString(5, venue.getState());
                preparedStatement.setString(6, venue.getZip());
                preparedStatement.setString(7, venue.getPhone());
                preparedStatement.setString(8, venue.getEmail());
                preparedStatement.setString(9, venue.getWebsite());
                preparedStatement.setString(10, venue.getSource());
                preparedStatement.setString(11, venue.getGenre());
                if (venue.getCapacity() != null) {
                    preparedStatement.setInt(12, venue.getCapacity());
                } else {
                    preparedStatement.setNull(12, Types.INTEGER);
                }
                if (venue.getMinAge() != null) {
                    preparedStatement.setInt(13, venue.getMinAge());
                } else {
                    preparedStatement.setNull(13, Types.INTEGER);
                }
                preparedStatement.setString(14, venue.getNotes());
                preparedStatement.addBatch();
                if (++batchSize == 100) {
                    preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                    batchSize = 0;
                }
            }

            if (batchSize > 0) {
                preparedStatement.executeBatch();
                preparedStatement.clearBatch();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

}
