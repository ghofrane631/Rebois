package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.entity.AlerteEmise;

import java.util.List;

public interface AlerteEmiseRepository extends JpaRepository<AlerteEmise, Long> {

    List<AlerteEmise> findByIncident_Id(Long incidentId);

    List<AlerteEmise> findByUserId(String userId);

    List<AlerteEmise> findByUserIdAndLue(String userId, boolean lue);

    List<AlerteEmise> findByLueFalse();
}
