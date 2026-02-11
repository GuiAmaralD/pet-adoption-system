package com.example.auth.Pet;

import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}

