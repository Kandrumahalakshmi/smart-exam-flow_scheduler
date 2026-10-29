
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
        name = "room_equipment",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"room_id", "equipment_type"}
                )
        }
)
public class RoomEquipment {

    @Id
    @Column(length = 40, nullable = false, unique = true)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotBlank
    @Column(name = "equipment_type", nullable = false, length = 50)
    private String equipmentType;

    @Min(0)
    @Column(nullable = false)
    private int quantity = 1;

    public RoomEquipment() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
