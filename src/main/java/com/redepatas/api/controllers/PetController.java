package com.redepatas.api.controllers;

import com.redepatas.api.dtos.petDtos.DeletePetDto;
import com.redepatas.api.dtos.petDtos.GetPetsDto;
import com.redepatas.api.dtos.petDtos.NewPetDto;
import com.redepatas.api.dtos.petDtos.PetUpdateDto;
import com.redepatas.api.dtos.petDtos.VacinaDto;
import com.redepatas.api.models.PetModel;
import com.redepatas.api.services.PetServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pets")
public class PetController {

    @Autowired
    private PetServices petServices;

    @PostMapping(value = "/newPet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> addPet(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("petDto") @Validated NewPetDto petDto,
            @RequestPart("file") MultipartFile file) {

        String login = userDetails.getUsername();
        String response = petServices.addPetToClient(login, petDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/petsByClient")
    public ResponseEntity<List<GetPetsDto>> getPetsByClient(@AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        List<GetPetsDto> pets = petServices.getPetsByLogin(login);
        if (pets.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/getAll") // REMOVER DEPOIS OU FAZER PASSAR POR UM DTO
    public ResponseEntity<List<PetModel>> getAllPets() {
        List<PetModel> pets = petServices.getAllPets();
        return ResponseEntity.ok(pets);
    }

    @PutMapping("/updatePet/{id}")
    public ResponseEntity<String> updatePet(
            @PathVariable UUID id,
            @RequestBody @Validated PetUpdateDto petDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        String response = petServices.updatePet(login, id, petDto);
        if (response.equals("Pet não encontrado!")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletePet/{id}")
    public ResponseEntity<String> deletePet(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        String response = petServices.deletePet(login, id);
        if (response.equals("Pet não encontrado!")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addVacina/{petId}")
    public ResponseEntity<String> addVacinaToPet(
            @PathVariable UUID petId,
            @RequestBody @Validated VacinaDto vacinaDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        String response = petServices.addVacinaToPet(login, petId, vacinaDto);
        if (response.equals("Pet não encontrado!")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteVacina")
    public ResponseEntity<String> deleteVacinaFromPet(
            @RequestBody @Validated DeletePetDto petDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String login = userDetails.getUsername();
        String response = petServices.deleteVacinaToPet(login, petDto.petId(), petDto.vacinaId());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}