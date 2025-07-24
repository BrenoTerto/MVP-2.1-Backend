package com.redepatas.api.cliente.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.redepatas.api.cliente.dtos.ChangePasswordDto;
import com.redepatas.api.cliente.dtos.UserDtos.AddAddressDto;
import com.redepatas.api.cliente.dtos.UserDtos.ClientResponseDTO;
import com.redepatas.api.cliente.dtos.UserDtos.EnderecoDto;
import com.redepatas.api.cliente.dtos.UserDtos.UpdateProfileDto;
import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.Endereco;
import com.redepatas.api.cliente.repositories.ClientRepository;

@Service
public class UserServices {

    @Autowired
    private ClientRepository repositoryUser;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private FileService fileService;
    @Autowired
    IS3Service is3Service;

    public String newAddress(String login, AddAddressDto addressDto) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            return "Usuário não encontrado";
        }
        var client = (ClientModel) user;
        for (Endereco endereco : client.getEnderecos()) {
            endereco.setSelecionado(false);
        }
        var endereco = new Endereco(addressDto.rua(), addressDto.bairro(), addressDto.cidade(), addressDto.estado(),
                addressDto.cep(),
                addressDto.numero(), addressDto.complemento(), addressDto.lugar());
        client.getEnderecos().add(endereco);
        endereco.setClientModel(client);
        repositoryUser.save(client);
        return "Endereço adicionado com sucesso";
    }

    public String deleteAddress(String login, UUID id) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            return "Usuário não encontrado";
        }
        var client = (ClientModel) user;
        var endereco = client.getEnderecos().stream().filter(e -> e.getIdEndereco().equals(id)).findFirst();
        if (endereco.isPresent()) {
            client.getEnderecos().remove(endereco.get());
            repositoryUser.save(client);
            return "Endereço removido com sucesso";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este endereço não pertence ao cliente.");
        }
    }

    public String updateAddress(String login, UUID id, AddAddressDto addressDto) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            return "Usuário não encontrado";
        }
        var client = (ClientModel) user;
        var endereco = client.getEnderecos().stream().filter(e -> e.getIdEndereco().equals(id)).findFirst();
        if (endereco.isPresent()) {
            var end = endereco.get();
            if (addressDto.rua() != null)
                end.setRua(addressDto.rua());
            if (addressDto.cidade() != null)
                end.setCidade(addressDto.cidade());
            if (addressDto.estado() != null)
                end.setEstado(addressDto.estado());
            if (addressDto.cep() != null)
                end.setCep(addressDto.cep());
            if (addressDto.numero() != null)
                end.setNumero(addressDto.numero());
            if (addressDto.complemento() != null)
                end.setComplemento(addressDto.complemento());
            repositoryUser.save(client);
            return "Endereço atualizado com sucesso";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este endereço não pertence ao cliente.");
        }
    }

    public ClientResponseDTO getUser(String login) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

        var client = (ClientModel) user;

        List<EnderecoDto> enderecoDTOs = client.getEnderecos().stream()
                .map(endereco -> new EnderecoDto(
                        endereco.getIdEndereco().toString(),
                        endereco.getRua(),
                        endereco.getCidade(),
                        endereco.getEstado(),
                        endereco.getBairro(),
                        endereco.getCep(),
                        endereco.getComplemento(),
                        endereco.getNumero(),
                        endereco.getLugar(),
                        endereco.getSelecionado()))
                .toList();

        return new ClientResponseDTO(
                client.getPhotoUrl(),
                client.getCPF(),
                client.getName(),
                client.getBirthDate(),
                client.getLogin(),
                client.getEmail(),
                client.getPhoneNumber(),
                enderecoDTOs);
    }

    public List<EnderecoDto> getAddressesByLogin(String login) {
        var user = (ClientModel) repositoryUser.findByLogin(login);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        return user.getEnderecos().stream()
                .map(endereco -> new EnderecoDto(
                        endereco.getIdEndereco().toString(),
                        endereco.getRua(),
                        endereco.getCidade(),
                        endereco.getEstado(),
                        endereco.getBairro(),
                        endereco.getCep(),
                        endereco.getComplemento(),
                        endereco.getNumero(),
                        endereco.getLugar(),
                        endereco.getSelecionado()))
                .toList();
    }

    public String updateProfile(String login, UpdateProfileDto updateProfileDto) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        var client = (ClientModel) user;
        if (updateProfileDto.name() != null) {
            client.setName(updateProfileDto.name());
        }
        if (updateProfileDto.CPF() != null) {
            client.setCPF(updateProfileDto.CPF());
        }
        if (updateProfileDto.birthDate() != null) {
            client.setBirthDate(updateProfileDto.birthDate());
        }
        repositoryUser.save(client);
        return "Perfil atualizado com sucesso";
    }

    public String changePassword(String login, ChangePasswordDto data) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario não encontrado!");
        }
        try {
            var auth = new UsernamePasswordAuthenticationToken(login, data.oldPassword());
            authenticationManager.authenticate(auth);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha antiga incorreta!");
        }
        var client = (ClientModel) user;
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.newPassword());
        client.setPassword(encryptedPassword);
        repositoryUser.save(client);

        return "Senha alterada com sucesso";
    }

    public String recoveryPassword(String login, String novaSenha) {
        var user = repositoryUser.findByLogin(login);
        var client = (ClientModel) user;
        String encryptedPassword = new BCryptPasswordEncoder().encode(novaSenha);
        client.setPassword(encryptedPassword);
        repositoryUser.save(client);
        return "Senha alterada com sucesso";
    }

    public String changeAvatar(String login, MultipartFile file) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario não encontrado!");
        }
        try {
            MultipartFile webpFile = fileService.convertToWebP(file);
            Map<String, String> response = is3Service.uploadFile(webpFile, "users").getBody();
            if (response == null || !response.containsKey("fileUrl")) {
                throw new RuntimeException("Erro ao fazer upload da imagem para a AWS.");
            }

            String imageUrl = response.get("fileUrl");
            var client = (ClientModel) user;
            client.setPhotoUrl(imageUrl);
            repositoryUser.save(client);
            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem para o avatar.", e);
        }
    }

    public String selecionarEndereco(String login, UUID idEndereco) {
        var user = repositoryUser.findByLogin(login);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado!");
        }

        var client = (ClientModel) user;

        boolean encontrouEndereco = false;

        for (Endereco endereco : client.getEnderecos()) {
            if (endereco.getIdEndereco().equals(idEndereco)) {
                endereco.setSelecionado(true);
                encontrouEndereco = true;
            } else {
                endereco.setSelecionado(false);
            }
        }

        if (!encontrouEndereco) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este endereço não pertence ao cliente.");
        }

        repositoryUser.save(client);
        return "Endereço selecionado com sucesso!";
    }

}