module said.microgest {
    requires javafx.controls;
    requires javafx.fxml;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.postgresql.jdbc;

    requires jbcrypt;

    requires static lombok;
    requires com.github.librepdf.openpdf;
    requires jakarta.mail;
    requires java.desktop;

    opens said.microgest to javafx.fxml;
    opens said.microgest.controllers to javafx.fxml;
    opens said.microgest.entities to javafx.base, javafx.fxml, org.hibernate.orm.core;
    opens said.microgest.enums to javafx.base, javafx.fxml;
    opens said.microgest.utils to javafx.base, javafx.fxml;
    opens said.microgest.services to javafx.fxml;
    opens said.microgest.repositories to javafx.fxml;
    opens said.microgest.config to javafx.fxml;

    exports said.microgest;
    exports said.microgest.controllers;
    exports said.microgest.entities;
    exports said.microgest.enums;
    exports said.microgest.utils;
    exports said.microgest.services;
    exports said.microgest.repositories;
    exports said.microgest.config;
}