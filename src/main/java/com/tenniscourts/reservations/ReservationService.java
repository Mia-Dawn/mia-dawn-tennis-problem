package com.tenniscourts.reservations;

import com.tenniscourts.config.persistence.BaseEntity;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final ScheduleRepository scheduleRepository;

    private final ReservationMapper reservationMapper;

    /* This _could_ verify if there was a schedule overlapping for this user with the one they're
       trying to reserve now... but, for now, that is on them if they schedule two at the same time
       and we'll just be keeping their deposit for one.
     */
    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        Guest guest = guestRepository.findById(createReservationRequestDTO.getGuestId()).orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found.");
        });
        Schedule schedule = scheduleRepository.findById(createReservationRequestDTO.getScheduleId()).orElseThrow(() -> {
            throw new EntityNotFoundException("Schedule not found.");
        });

        Reservation reservation = validateAndBuildReservation(guest, schedule);

        return reservationMapper.map(reservationRepository.save(reservation));
    }

    private Reservation validateAndBuildReservation(Guest guest, Schedule schedule) {
        if(reservationRepository.findBySchedule_Id(schedule.getId()).stream()
                .anyMatch(reservation -> reservation.getGuest().getId().equals(guest.getId()))) {
            throw new IllegalArgumentException("Guest is already reserved on this schedule.");
        }

        if(schedule.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule a reservation in the past.");
        }

        return Reservation.builder().guest(guest).schedule(schedule).reservationStatus(ReservationStatus.READY_TO_PLAY).value(BigDecimal.TEN).build();
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservationMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    /* I am pretty unhappy with what I had to do here, but am unsure how to generate a list of reservations
       based on their schedule ids using a repository method.
     */
    public List<ReservationDTO> findAllReservationsBetweenTimes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Long> scheduleIds = scheduleRepository.findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(startDateTime, endDateTime)
                .stream().map(BaseEntity::getId).collect(Collectors.toList());

        return reservationRepository.findAll().stream()
                .filter(reservation -> scheduleIds.contains(reservation.getSchedule().getId()))
                .map(reservationMapper::map).collect(Collectors.toList());
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(reservationId));
    }

    private Reservation cancel(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {

            this.validateCancellationOrRescheduling(reservation);

            return reservationRepository.save(
                    this.updateReservation(reservation, ReservationStatus.CANCELLED));
        }).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    private Reservation updateReservation(Reservation reservation, ReservationStatus status) {
        BigDecimal refundValue = getRefundValue(reservation);
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservation;
    }

    private void validateCancellationOrRescheduling(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        if (reservation.getSchedule().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }

    /* The requirements say deposits are to be per court, but they live on the reservation
       rather than the schedule initially. I am going to assume that it is a per user charge,
       as that is what the initial implementation implies, and I apologize if that is incorrect.
     */
    public BigDecimal getRefundValue(Reservation reservation) {
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), reservation.getSchedule().getStartDateTime());

        if (minutes >= (24 * 60)) {
            return reservation.getValue();
        } else if (minutes >= (12 * 60)) {
            return reservation.getValue().multiply(BigDecimal.valueOf(.75d));
        } else if (minutes >= (2 * 60)) {
            return reservation.getValue().multiply(BigDecimal.valueOf(.5d));
        } else if (minutes >= 1) {
            return reservation.getValue().multiply(BigDecimal.valueOf(.25d));
        }

        return BigDecimal.ZERO;
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long scheduleId) {
        Reservation previousReservation = reservationRepository
                .findById(previousReservationId).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation to reschedule not found.");
        });

        if (scheduleId.equals(previousReservation.getSchedule().getId())) {
            throw new IllegalArgumentException("Cannot reschedule to the same slot.");
        }

        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> {
            throw new EntityNotFoundException("Schedule not found.");
        });


        validateCancellationOrRescheduling(previousReservation);
        updateReservation(previousReservation, ReservationStatus.RESCHEDULED);

        Reservation newReservation = validateAndBuildReservation(previousReservation.getGuest(), schedule);

        List<Reservation> reservationList = new ArrayList<>() {{add(previousReservation); add(newReservation);}};

        /* Some implementations of saveAll do all this in one transaction;
           I'm not sure if its useful here but I'm trying to make this as atomic an operation
           as possible. It makes the code below far messier though, so maybe its not worth it?
         */
        List<Reservation> savedReservations = reservationRepository.saveAll(reservationList);

        ReservationDTO newReservationDTO = null;
        ReservationDTO previousReservationDTO = null;

        for (Reservation r: savedReservations) {
            if(r.getSchedule().getId().equals(scheduleId)) {
                newReservationDTO = reservationMapper.map(r);
            } else {
                previousReservationDTO = reservationMapper.map(r);
            }
        }

        if (null == newReservationDTO || null == previousReservationDTO) {
            throw new EntityNotFoundException("Reservations weren't updated correctly."); //Shouldn't happen, but here for safety.
            // Could use a better type of exception, probably, but not sure what.
        }

        newReservationDTO.setPreviousReservation(previousReservationDTO);
        return newReservationDTO;
    }
}
