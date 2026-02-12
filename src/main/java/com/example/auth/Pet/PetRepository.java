package com.example.auth.Pet;

import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long>, JpaSpecificationExecutor<Pet> {
    List<Pet> findAllByAdoptedFalse();

    @Override
    Optional<Pet> findById(Long id);

    boolean existsByUserAndNicknameAndSizeAndSpecieAndDescriptionAndSex(
            User user,
            String nickname,
            Size size,
            Specie specie,
            String description,
            Sex sex
    );

    @Query("""
            SELECT p FROM Pet p
            WHERE (:specie IS NULL OR p.specie = :specie)
            AND (:sex IS NULL OR p.sex = :sex)
            AND (:size IS NULL OR p.size = :size)
           """)
    List<Pet> findByFilters(
            @Param("specie") Specie specie,
            @Param("sex") Sex sex,
            @Param("size") Size size
    );
}

