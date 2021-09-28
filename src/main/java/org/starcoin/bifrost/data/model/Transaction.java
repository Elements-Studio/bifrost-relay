package org.starcoin.bifrost.data.model;

public interface Transaction {

   String STATUS_CREATED = "CREATED";

    String STATUS_SENT = "SENT";

    String STATUS_CONFIRMED = "CONFIRMED";

    String STATUS_DROPPED = "DROPPED";

    String STATUS_CANCELED = "CANCELED";

    String STATUS_TOMBSTONED = "TOMBSTONED";
    
}
