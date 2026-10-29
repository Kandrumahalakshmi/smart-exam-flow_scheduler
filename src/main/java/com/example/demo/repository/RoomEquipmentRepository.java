
package com.example.demo.repository;

import com.example.demo.model.RoomEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomEquipmentRepository
        extends JpaRepository<RoomEquipment, String> {

    List<RoomEquipment> findByRoom_Id(String roomId);
}

