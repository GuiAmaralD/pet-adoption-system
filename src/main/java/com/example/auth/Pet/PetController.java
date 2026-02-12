package com.example.auth.Pet;


import com.example.auth.Pet.DTOs.RegisterPetDTO;
import com.example.auth.Pet.DTOs.PetResponseDTO;
import com.example.auth.Pet.enums.Sex;
import com.example.auth.Pet.enums.Size;
import com.example.auth.Pet.enums.Specie;
import com.example.auth.user.User;
import com.example.auth.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pet")
@CrossOrigin("*")
public class PetController {

    private final PetService petService;
    private final UserService userService;

    public PetController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> findAllByAdoptedFalse() {
        List<Pet> pets = petService.findAllByAdoptedFalse();
        List<PetResponseDTO> dtos = pets.stream()
                .map(petService::toSendPetToClientDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(dtos);
    }

    @GetMapping("/filter")
    public List<PetResponseDTO> getPetsByCriteria(
            @RequestParam(required = false) Specie specie,
            @RequestParam(required = false) Sex sex,
            @RequestParam(required = false) Size size
    ) {
        return petService.findByFilters(specie, sex, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPet(@PathVariable Long id) {
        Pet pet = petService.findById(id);

        PetResponseDTO dto = petService.toSendPetToClientDTO(pet);
        return ResponseEntity.ok(dto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetResponseDTO> registerNewPet(
            @RequestPart("pet") @Valid RegisterPetDTO dto,
            @RequestPart(value = "images") List<MultipartFile> images,
            Principal principal) throws IOException {

        User user = (User) userService.findByEmail(principal.getName());
        PetResponseDTO response = petService.registerNewPet(dto, images, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/adopted")
    public ResponseEntity<Pet> setAdoptedTrue(@PathVariable Long id, Principal principal){
        Pet pet = petService.findById(id);
        if(petService.isPetFromLoggedUser(id, principal)){
            pet.setAdopted(true);
            petService.save(pet);
        }else{
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You can only update your pets data");
        }
        return ResponseEntity.ok().build();
    }
}
