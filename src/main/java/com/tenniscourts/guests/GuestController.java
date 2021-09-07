package com.tenniscourts.guests;

import com.tenniscourts.config.BaseRestController;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

@AllArgsConstructor
@RestController
public class GuestController extends BaseRestController {

    private final GuestService guestService;

    @PostMapping("/guest")
    public ResponseEntity<Void> addGuest(@RequestBody @Valid CreateGuestRequestDTO guestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.addGuest(guestDTO).getId())).build();
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<GuestDTO> getGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestService.findGuestById(guestId));
    }

    @GetMapping("/guest/name/{guestName}")
    public ResponseEntity<GuestDTO> getGuestByName(@PathVariable String guestName) {
        return ResponseEntity.ok(guestService.findGuestByName(guestName));
    }

    @GetMapping("/guest/list")
    public ResponseEntity<Collection<GuestDTO>> getGuestList() {
        return ResponseEntity.ok(guestService.findAllGuests());
    }

    @DeleteMapping("/guest/{guestId}")
    public ResponseEntity<GuestDTO> deleteGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestService.deleteGuest(guestId));
    }

    @PutMapping("/guest")
    public ResponseEntity<GuestDTO> updateGuest(@RequestBody @Valid GuestDTO guestDTO) {
        return ResponseEntity.ok(guestService.updateGuest(guestDTO));
    }
}
