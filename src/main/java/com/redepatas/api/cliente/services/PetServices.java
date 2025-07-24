package com.redepatas.api.cliente.services;

import com.redepatas.api.cliente.dtos.petDtos.GetPetsDto;
import com.redepatas.api.cliente.dtos.petDtos.NewPetDto;
import com.redepatas.api.cliente.dtos.petDtos.PetUpdateDto;
import com.redepatas.api.cliente.dtos.petDtos.VacinaDto;
import com.redepatas.api.cliente.models.ClientModel;
import com.redepatas.api.cliente.models.PetModel;
import com.redepatas.api.cliente.models.Vacina;
import com.redepatas.api.cliente.repositories.ClientRepository;
import com.redepatas.api.cliente.repositories.PetRepository;
import com.redepatas.api.parceiro.repositories.AgendamentoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PetServices {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    IS3Service is3Service;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    public String updatePet(String login, UUID petId, PetUpdateDto dto) {

        PetModel pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet não encontrado!"));

        if (!pet.getClient().getLogin().equals(login)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pet não pertence ao cliente.");
        }

        try {
            if (dto.avatarUrl() != null) {
                pet.setAvatarUrl(dto.avatarUrl());
            }
            if (dto.nome() != null) {
                pet.setNome(dto.nome());
            }
            if (dto.especie() != null) {
                pet.setEspecie(dto.especie());
            }
            if (dto.raca() != null) {
                pet.setRaca(dto.raca());
            }
            if (dto.observacoes() != null) {
                pet.setObservacoes(dto.observacoes());
            }
            if (dto.castrado() != null) {
                pet.setCastrado(dto.castrado());
            }
            if (dto.sociavel() != null) {
                pet.setSociavel(dto.sociavel());
            }
            if (dto.dataNascimento() != null) {
                pet.setDataNascimento(dto.dataNascimento());
            }
            if (dto.sexo() != null) {
                pet.setSexo(dto.sexo());
            }
            if (dto.peso() != null) {
                pet.setPeso(dto.peso());
            }
            if (dto.tipoSanguineo() != null) {
                pet.setTipoSanguineo(dto.tipoSanguineo());
            }
            if (dto.porte() != null) {
                pet.setPorte(dto.porte());
            }

            // Salva as alterações feitas no pet
            petRepository.save(pet);
            return "Pet atualizado com sucesso!";
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RG do pet já existe.");
        }
    }

    public String deletePet(String login, UUID petId) {
        ClientModel client = (ClientModel) clientRepository.findByLogin(login);
        PetModel pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pet não encontrado ou não pertence ao cliente."));

        if (!pet.getClient().getIdUser().equals(client.getIdUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pet não pertence ao cliente.");
        }

        boolean petTemAgendamento = agendamentoRepository.existsByPetModel(pet);
        if (petTemAgendamento) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Este pet está vinculado a agendamentos e não pode ser excluído.");
        }

        petRepository.deleteById(petId);
        return "Pet removido com sucesso!";
    }

    private PetModel convertDtoToPet(NewPetDto dto, String urlPet) {
        PetModel pet = new PetModel();
        pet.setRgPet(dto.rgPet());
        pet.setAvatarUrl(urlPet);
        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setObservacoes(dto.observacoes());
        pet.setCastrado(dto.castrado());
        pet.setSociavel(dto.sociavel());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setSexo(dto.sexo());
        pet.setPeso(dto.peso());
        pet.setTipoSanguineo(dto.tipoSanguineo());
        pet.setPorte(dto.porte());

        if (dto.vacinas() != null) {
            List<Vacina> vacinas = dto.vacinas().stream().map(v -> {
                Vacina vacina = new Vacina();
                vacina.setNome(v.nome());
                vacina.setDataAplicacao(v.dataAplicacao());
                return vacina;
            }).toList();
            pet.setVacinas(vacinas);
        }

        return pet;
    }

    public List<GetPetsDto> getPetsByLogin(String login) {
        ClientModel client = (ClientModel) clientRepository.findByLogin(login);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!");
        }

        List<PetModel> pets = petRepository.findAllByClient_IdUser(client.getIdUser());
        if (pets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum pet encontrado para este cliente.");
        }

        return pets.stream()
                .map(this::convertToGetPetsDto)
                .toList();
    }

    private GetPetsDto convertToGetPetsDto(PetModel pet) {
        return new GetPetsDto(
                pet.getIdPet(),
                pet.getRgPet(),
                pet.getAvatarUrl(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getObservacoes(),
                pet.getCastrado(),
                pet.getSociavel(),
                convertToVacinaDto(pet.getVacinas()), // Converte a lista de Vacinas, se necessário
                pet.getDataNascimento(),
                pet.getSexo(),
                pet.getPeso(),
                pet.getTipoSanguineo(),
                pet.getPorte());
    }

    private List<VacinaDto> convertToVacinaDto(List<Vacina> vacinas) {
        if (vacinas == null) {
            return List.of(); // Retorna uma lista vazia caso não haja vacinas
        }

        return vacinas.stream()
                .map(vacina -> new VacinaDto(
                        vacina.getNome(),
                        vacina.getDataAplicacao()))
                .toList();
    }

    public List<PetModel> getAllPets() {
        return petRepository.findAll();
    }

    public String addPetToClient(String login, NewPetDto dto, MultipartFile file) {
        ClientModel client = (ClientModel) clientRepository.findByLogin(login);
        String imageUrl;
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado!");
        }
        try {
            MultipartFile webpFile = fileService.convertToWebP(file);
            Map<String, String> response = is3Service.uploadFile(webpFile, "pets").getBody();
            if (response == null || !response.containsKey("fileUrl")) {
                throw new RuntimeException("Erro ao fazer upload da imagem para a AWS.");
            }

            imageUrl = response.get("fileUrl");
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem para o avatar.", e);
        }
        try {
            PetModel pet = convertDtoToPet(dto, imageUrl);
            pet.setClient(client);
            pet.setIdPet(null);

            petRepository.save(pet);
            return "Pet adicionado ao cliente com sucesso!";
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RG do pet já existe.");
        }
    }

    public String addVacinaToPet(String login, UUID petId, VacinaDto vacinaDto) {
        PetModel pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet não encontrado!"));

        if (!pet.getClient().getLogin().equals(login)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este pet não pertence ao cliente.");
        }
        Vacina vacina = new Vacina();
        vacina.setNome(vacinaDto.nome());
        vacina.setDataAplicacao(vacinaDto.dataAplicacao());
        pet.getVacinas().add(vacina);

        petRepository.save(pet);
        return "Vacina adicionada ao pet com sucesso!";
    }

    public String deleteVacinaToPet(String login, UUID petId, UUID vacinaId) {
        ClientModel client = (ClientModel) clientRepository.findByLogin(login);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente não encontrado.");
        }

        PetModel pet = client.getPets().stream()
                .filter(p -> p.getIdPet().equals(petId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pet não encontrado ou não pertence ao cliente."));

        Vacina vacina = pet.getVacinas().stream()
                .filter(v -> v.getIdVacina().equals(vacinaId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vacina não encontrada."));

        pet.getVacinas().remove(vacina);
        petRepository.save(pet);

        return "Vacina removida do pet com sucesso!";
    }

}