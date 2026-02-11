package com.example.auth.Pet;

import com.example.auth.Pet.DTOs.RegisterPetDTO;
import com.example.auth.Pet.DTOs.PetResponseDTO;
import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.User;
import com.example.auth.user.services.UserService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final UserService userService;
    private final SupabaseStorageService supabaseStorageService;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/gif"
    );


    public PetService(PetRepository petRepository, UserService userService, SupabaseStorageService supabaseStorageService) {
        this.petRepository = petRepository;
        this.userService = userService;
        this.supabaseStorageService = supabaseStorageService;
    }

    public List<Pet> findAllByAdoptedFalse(){
        List<Pet> pets = petRepository.findAllByAdoptedFalse();
        return pets;
    }

    public Pet findById(Long id){
        return petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Pet with such Id not found"));
    }

    public List<Pet> findPetsByCriteria(String specie, String sex, String size) {
        return petRepository.findAll((Specification<Pet>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(specie)) {
                predicates.add(criteriaBuilder.equal(
                        root.get("specie"),
                        Specie.valueOf(specie.toUpperCase())
                ));
            }
            if (StringUtils.hasText(sex)) {
                predicates.add(criteriaBuilder.equal(
                        root.get("sex"),
                        Sex.valueOf(sex.toUpperCase())
                ));
            }
            if (StringUtils.hasText(size)) {
                predicates.add(criteriaBuilder.equal(
                        root.get("size"),
                        Size.valueOf(size.toUpperCase())
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    @Transactional
    public Pet save(Pet pet){
        return petRepository.save(pet);
    }

    public PetResponseDTO registerNewPet(RegisterPetDTO dto, List<MultipartFile> images, User user) throws IOException {
        Pet pet = new Pet(null,
                dto.nickname(),
                dto.sex(),
                dto.description(),
                dto.size(),
                new Date(System.currentTimeMillis()),
                false,
                dto.specie(),
                user);

        List<String> imageUrls = processImages(images);
        pet.setImageUrls(imageUrls);

        petRepository.save(pet);

        return new PetResponseDTO(
                pet.getId(),
                pet.getNickname(),
                pet.getSex(),
                pet.getSize(),
                pet.getSpecie(),
                pet.getDescription(),
                pet.getUser(),
                pet.getImageUrls()
        );
    }

    private List<String> processImages(List<MultipartFile> images) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        if (images != null) {
            if (images.size() > 4)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "each pet has a limit of 4 images");

            Set<String> hashes = new HashSet<>();

            for (MultipartFile image : images) {
                if (image.getSize() > 10 * 1024 * 1024)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "image file " + image.getOriginalFilename() + " is too big");

                if (!ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid file type: " + image.getOriginalFilename());
                }

                String hash = DigestUtils.md5DigestAsHex(image.getBytes());
                if (!hashes.add(hash)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "duplicate file detected: " + image.getOriginalFilename());
                }

                String url = supabaseStorageService.uploadFile("pet-images", image);
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    public boolean isPetFromLoggedUser(Long id, Principal principal){
        User user = (User) userService.findByEmail(principal.getName());

        Pet pet = this.findById(id);

        if(user.getRegisteredPets().contains(pet)){
            return true;
        }
        return false;
    }


    public PetResponseDTO toSendPetToClientDTO(Pet pet) {

        return new PetResponseDTO(
                pet.getId(),
                pet.getNickname(),
                pet.getSex(),
                pet.getSize(),
                pet.getSpecie(),
                pet.getDescription(),
                pet.getUser(),
                pet.getImageUrls()
        );
    }


}
