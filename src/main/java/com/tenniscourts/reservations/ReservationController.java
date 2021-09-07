package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    @PostMapping("/reservation")
    public ResponseEntity<Void> bookReservation(@RequestBody @Valid CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }

    //TODO: Multiple reservations? Maybe multiple on a single schedule?

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ReservationDTO> findReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.findReservation(reservationId));
    }

    @GetMapping("/reservation/list/{startDateTime}/{endDateTime}")
    public ResponseEntity<List<ReservationDTO>> findAllReservations(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") LocalDateTime startDateTime,
                                                                    @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") LocalDateTime endDateTime) {
        return ResponseEntity.ok(reservationService.findAllReservationsBetweenTimes(startDateTime, endDateTime));
    }

    @DeleteMapping("/reservation/{reservationId}")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }

    @PutMapping("/reservation/{reservationId}/{scheduleId}")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@PathVariable Long reservationId, @PathVariable Long scheduleId) {
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, scheduleId));
    }
}
