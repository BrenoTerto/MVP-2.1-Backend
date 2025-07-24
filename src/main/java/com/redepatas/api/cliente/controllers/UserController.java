package com.redepatas.api.cliente.controllers;

import com.redepatas.api.cliente.dtos.UserDtos.AddAddressDto;
import com.redepatas.api.cliente.dtos.UserDtos.EnderecoDto;
import com.redepatas.api.cliente.dtos.UserDtos.UpdateProfileDto;
import com.redepatas.api.cliente.services.GoogleGeoUtils;
import com.redepatas.api.cliente.services.UserServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private GoogleGeoUtils googleGeoUtils;

    @PutMapping("/updateAddress/{idAddress}")
    public ResponseEntity<String> updateAddress(
            @PathVariable("idAddress") UUID idAddress,
            @RequestBody @Validated AddAddressDto addressDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        String response = userServices.updateAddress(login, idAddress, addressDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/newAddress")
    public ResponseEntity<String> newAddress(
            @RequestBody @Validated AddAddressDto addressDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        String response = userServices.newAddress(login, addressDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteAddress/{idAddress}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable("idAddress") UUID idAddress,
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        String response = userServices.deleteAddress(login, idAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        var user = userServices.getUser(login);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/myAddress")
    public ResponseEntity<List<EnderecoDto>> getMyAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        String login = userDetails.getUsername();
        List<EnderecoDto> enderecos = new ArrayList<>(userServices.getAddressesByLogin(login));

        if (latitude != null && longitude != null) {
            EnderecoDto enderecoAtual = googleGeoUtils.reverseGeocode(latitude, longitude);
            enderecos.add(0, enderecoAtual);
        }

        return ResponseEntity.ok(enderecos);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<String> updateProfile(
            @RequestBody @Validated UpdateProfileDto updateProfileDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        String response = userServices.updateProfile(login, updateProfileDto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/changeAvatar")
    public ResponseEntity<Map<String, String>> changeAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        String login = userDetails.getUsername();
        String imageUrl = userServices.changeAvatar(login, file);
        Map<String, String> response = new HashMap<>();
        response.put("photoUrl", imageUrl);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/selectAddress/{idAddress}")
    public String selecionarEndereco(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("idAddress") UUID idAddress) {
        String login = userDetails.getUsername();
        return userServices.selecionarEndereco(login, idAddress);
    }

}
