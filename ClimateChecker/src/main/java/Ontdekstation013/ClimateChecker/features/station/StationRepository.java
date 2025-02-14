package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StationRepository extends JpaRepository <Station, Long> {
    Station getByRegistrationCode(Long meetstationCode);
    Station getMeetstationByStationid(Long id);

    List<Station> findAll();
    List<Station> findByUserid(Long userid);

    @Query("SELECT s FROM Station s WHERE " +
            "(:name IS NULL OR s.name LIKE %:name%) AND " +
            "(:databaseTag IS NULL OR s.database_tag LIKE %:databaseTag%) AND " +
            "(:isPublic IS NULL OR s.is_public = :isPublic) AND " +
            "(:registrationCode IS NULL OR s.registrationCode = :registrationCode) AND " +
            "(COALESCE(:userIds, NULL) IS NULL OR s.userid IN :userIds) AND " +
            "(:isActive IS NULL OR s.isActive = :isActive)")
    List<Station> findStationsByOptionalFilters(@Param("name") String name,
                                                @Param("databaseTag") String databaseTag,
                                                @Param("isPublic") Boolean isPublic,
                                                @Param("registrationCode") Long registrationCode,
                                                @Param("userIds") List<Long> userIds,
                                                @Param("isActive") Boolean isActive);


}
