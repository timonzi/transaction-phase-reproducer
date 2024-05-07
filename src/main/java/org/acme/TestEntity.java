package org.acme;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_test")
public class TestEntity {

    private String id;


    @Id
    public String getId() {
        return id;
    }


    public void setId(final String id) {
        this.id = id;
    }

}
