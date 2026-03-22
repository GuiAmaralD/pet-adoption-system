package com.example.auth.pet;


import com.example.auth.pet.DTOs.RegisterPetDTO;
import com.example.auth.pet.DTOs.PetResponseDTO;
import com.example.auth.pet.DTOs.UpdatePetDTO;
import com.example.auth.pet.enums.Sex;
import com.example.auth.pet.enums.Size;
import com.example.auth.pet.enums.Specie;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/pet")
@CrossOrigin("*")
@Tag(name = "Pets", description = "Pet registration and lookup operations")
public class PetController {

    private final PetService petService;
    private final UserService userService;

    public PetController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List available pets", description = "Returns pets that are not adopted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned")
    })
    public ResponseEntity<List<PetResponseDTO>> findAllByAdoptedFalse() {
        List<PetResponseDTO> pets = petService.findAllByAdoptedFalse();

        return ResponseEntity.ok().body(pets);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter pets", description = "Filters pets by species, sex, and size.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered list"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public List<PetResponseDTO> getPetsByCriteria(
            @RequestParam(required = false) Specie specie,
            @RequestParam(required = false) Sex sex,
            @RequestParam(required = false) Size size
    ) {
        return petService.findByFilters(specie, sex, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pet by ID", description = "Returns pet details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet found"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<PetResponseDTO> getPet(@PathVariable Long id) {
        PetResponseDTO pet = petService.findByIdAsDto(id);

        return ResponseEntity.ok(pet);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register pet", description = "Registers a pet with images.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet created"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Duplicate pet")
    })
    public ResponseEntity<PetResponseDTO> registerNewPet(
            @Parameter(description = "Pet data as JSON") @RequestPart("pet") @Valid RegisterPetDTO dto,
            @Parameter(description = "Images list (multipart)") @RequestPart(value = "images") List<MultipartFile> images,
            Principal principal) throws IOException {

        User user = (User) userService.findByEmail(principal.getName());
        PetResponseDTO response = petService.registerNewPet(dto, images, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update pet", description = "Updates a pet owned by the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet updated"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<PetResponseDTO> updatePet(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePetDTO dto,
            Principal principal) {
        PetResponseDTO response = petService.updatePet(id, dto, principal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/adopted")
    @Operation(summary = "Mark as adopted", description = "Marks a pet as adopted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet marked as adopted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<Void> setAdoptedTrue(@PathVariable Long id, Principal principal){
        Pet pet = petService.findById(id);
        if(petService.isPetFromLoggedUser(id, principal)){
            pet.setAdopted(true);
            petService.save(pet);
        }else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only update your pets data");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pet", description = "Removes a user's pet.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pet removed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Pet not found")
    })
    public ResponseEntity<Void> deletePet(@PathVariable Long id, Principal principal) {
        petService.deletePet(id, principal);
        return ResponseEntity.noContent().build();
    }
}
