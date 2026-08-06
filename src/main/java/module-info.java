module said.microgest {

    requires javafx.controls;
    requires javafx.fxml;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.postgresql.jdbc;

    requires jbcrypt;

    requires static lombok;

    opens said.microgest to javafx.fxml;
    opens said.microgest.controllers to javafx.fxml;
    opens said.microgest.entities to org.hibernate.orm.core;

    exports said.microgest;
    exports said.microgest.controllers;
}