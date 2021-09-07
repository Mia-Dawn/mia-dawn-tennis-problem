package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;

    public GuestDTO addGuest(CreateGuestRequestDTO guestDTO) {
        return guestMapper.map(guestRepository.saveAndFlush(guestMapper.map(guestDTO)));
    }

    public GuestDTO findGuestById(Long guestId) {
        return guestRepository.findById(guestId).map(guestMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found.");
        });
    }

    public GuestDTO findGuestByName(String guestName) {
        return guestRepository.findByName(guestName).map(guestMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found.");
        });
    }

    public Collection<GuestDTO> findAllGuests() {
        return guestRepository.findAll().stream().map(guestMapper::map).collect(Collectors.toList());
    }

    public GuestDTO deleteGuest(Long guestId) {
        GuestDTO guestDTO = guestRepository.findById(guestId).map(guestMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found.");
        });
        //FIXME: There might be issues involved with doing this; notably existing reservations will no longer
        // have guests associated, potentially causing issues
        guestRepository.deleteById(guestId);
        return guestDTO;
    }

    public GuestDTO updateGuest(GuestDTO guestDTO) {
        if (!guestRepository.existsById(guestDTO.getId())) {
            throw new EntityNotFoundException("Guest not found.");
        } else {
            return guestMapper.map(guestRepository.save(guestMapper.map(guestDTO)));
        }
    }
}
