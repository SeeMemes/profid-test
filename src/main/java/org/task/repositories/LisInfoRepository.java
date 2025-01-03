package org.task.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.task.entities.LisInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LisInfoRepository extends JpaRepository<LisInfoEntity, Long> {
    Optional<LisInfoEntity> findByName(String name);
    List<LisInfoEntity> findAllByNameIn(List<String> names);
}
