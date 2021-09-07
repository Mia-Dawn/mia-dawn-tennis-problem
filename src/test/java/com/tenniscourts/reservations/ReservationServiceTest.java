package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleRepository;
import com.tenniscourts.tenniscourts.TennisCourt;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = ReservationService.class)
public class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    GuestRepository guestRepository;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    ReservationMapper reservationMapper;

    @InjectMocks
    ReservationService reservationService;

    @Test
    public void bookReservationGuestNotFound() {
        when(guestRepository.findById(anyLong())).thenReturn(Optional.empty());

        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO
                .builder().guestId(1L).scheduleId(1L).build();

        assertThrows(EntityNotFoundException.class, () -> reservationService.bookReservation(createReservationRequestDTO));
    }

    @Test
    public void bookReservationScheduleNotFound() {
        Guest guest = Guest.builder().name("guestName").build();
        guest.setId(1L);
        when(guestRepository.findById(anyLong())).thenReturn(Optional.of(guest));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO
                .builder().guestId(1L).scheduleId(1L).build();

        assertThrows(EntityNotFoundException.class, () -> reservationService.bookReservation(createReservationRequestDTO));
    }

    @Test
    public void bookReservationGuestAlreadyReserved() {
        Guest guest = Guest.builder().name("guestName").build();
        guest.setId(1L);
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now()).endDateTime(LocalDateTime.now().plusHours(1)).build();
        schedule.setId(1L);
        Reservation reservation = Reservation.builder().schedule(schedule).guest(guest).build();
        reservation.setId(1L);

        when(guestRepository.findById(anyLong())).thenReturn(Optional.of(guest));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(reservationRepository.findBySchedule_Id(anyLong())).thenReturn(Collections.singletonList(reservation));

        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO
                .builder().guestId(1L).scheduleId(1L).build();

        assertThrows(IllegalArgumentException.class, () -> reservationService.bookReservation(createReservationRequestDTO));
    }

    @Test
    public void bookReservationScheduleInPast() {
        Guest guest = Guest.builder().name("guestName").build();
        guest.setId(1L);
        Guest otherGuest = Guest.builder().name("guestName").build();
        otherGuest.setId(2L);
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now()).endDateTime(LocalDateTime.now().plusHours(1)).build();
        schedule.setId(1L);
        Reservation reservation = Reservation.builder().schedule(schedule).guest(otherGuest).build();
        reservation.setId(1L);

        when(guestRepository.findById(anyLong())).thenReturn(Optional.of(guest));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(reservationRepository.findBySchedule_Id(anyLong())).thenReturn(Collections.singletonList(reservation));

        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO
                .builder().guestId(1L).scheduleId(1L).build();

        assertThrows(IllegalArgumentException.class, () -> reservationService.bookReservation(createReservationRequestDTO));
    }

    @Test
    public void bookReservationSuccess() {
        Guest guest = Guest.builder().name("guestName").build();
        guest.setId(1L);
        Guest otherGuest = Guest.builder().name("guestName").build();
        otherGuest.setId(2L);
        Schedule schedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(3)).endDateTime(LocalDateTime.now().plusHours(4)).build();
        schedule.setId(1L);
        Reservation reservation = Reservation.builder().schedule(schedule).guest(otherGuest).build();
        reservation.setId(1L);

        when(guestRepository.findById(anyLong())).thenReturn(Optional.of(guest));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(reservationRepository.findBySchedule_Id(anyLong())).thenReturn(Collections.singletonList(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = (Reservation) invocation.getArguments()[0];
            r.setId(1L);
            return r;
        });
        when(reservationMapper.map(any(Reservation.class))).thenAnswer(invocation ->
                ReservationDTO.builder().id(((Reservation) invocation.getArguments()[0]).getId())
                        .guestId(((Reservation) invocation.getArguments()[0]).getGuest().getId()).build());

        CreateReservationRequestDTO createReservationRequestDTO = CreateReservationRequestDTO
                .builder().guestId(1L).scheduleId(1L).build();

        assertEquals(1L, reservationService.bookReservation(createReservationRequestDTO).getId());
        assertEquals(1L, reservationService.bookReservation(createReservationRequestDTO).getGuestId());
    }

    @Test
    public void findReservationNotFound() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.findReservation(1L));
    }

    @Test
    public void findReservationSuccess() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(reservation));
        when(reservationMapper.map(any(Reservation.class))).thenAnswer(invocation ->
                ReservationDTO.builder().id(((Reservation) invocation.getArguments()[0]).getId()).build());

        assertEquals(1L, reservationService.findReservation(1L).getId());
    }

    @Test
    public void findReservationsBetweenTimesNoSchedulesFound() {
        List<Schedule> schedules = new ArrayList<>();
        List<Reservation> reservationList = new ArrayList<>();

        Schedule schedule1 = new Schedule();
        schedule1.setId(1L);
        Schedule schedule2 = new Schedule();
        schedule2.setId(2L);

        reservationList.add(Reservation.builder().schedule(schedule1).build());

        when(scheduleRepository.findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(schedules);
        when(reservationRepository.findAll()).thenReturn(reservationList);

        assertTrue(reservationService.findAllReservationsBetweenTimes(
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)).isEmpty());
    }

    @Test
    public void findReservationsBetweenTimesNoReservationsFound() {
        List<Schedule> schedules = new ArrayList<>();
        List<Reservation> reservationList = new ArrayList<>();

        Schedule schedule1 = new Schedule();
        schedule1.setId(1L);
        Schedule schedule2 = new Schedule();
        schedule2.setId(2L);

        schedules.add(schedule1);
        schedules.add(schedule2);

        when(scheduleRepository.findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(schedules);
        when(reservationRepository.findAll()).thenReturn(reservationList);

        assertTrue(reservationService.findAllReservationsBetweenTimes(
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)).isEmpty());
    }

    @Test
    public void findReservationsBetweenTimesSuccess() {
        List<Schedule> schedules = new ArrayList<>();
        List<Reservation> reservationList = new ArrayList<>();

        Schedule schedule1 = new Schedule();
        schedule1.setId(1L);
        Schedule schedule2 = new Schedule();
        schedule2.setId(2L);
        Schedule schedule3 = new Schedule();
        schedule3.setId(3L);

        schedules.add(schedule1);
        schedules.add(schedule2);

        reservationList.add(Reservation.builder().schedule(schedule1).build());
        reservationList.add(Reservation.builder().schedule(schedule3).build());

        when(scheduleRepository.findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(
                any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(schedules);
        when(reservationRepository.findAll()).thenReturn(reservationList);

        assertEquals(1, reservationService.findAllReservationsBetweenTimes(
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)).size());
    }

    @Test
    public void cancelReservationNotFound() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.cancelReservation(1L));
    }

    @Test
    public void cancelReservationNotReadyToPlay() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(
                Reservation.builder().reservationStatus(ReservationStatus.CANCELLED).build()));

        assertThrows(IllegalArgumentException.class, () -> reservationService.cancelReservation(1L));
    }

    @Test
    public void cancelReservationInPast() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(
                Reservation.builder().reservationStatus(ReservationStatus.READY_TO_PLAY)
                        .schedule(Schedule.builder().startDateTime(LocalDateTime.now()).build()).build()));

        assertThrows(IllegalArgumentException.class, () -> reservationService.cancelReservation(1L));
    }

    @Test
    public void cancelReservationSuccess() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(
                Reservation.builder().reservationStatus(ReservationStatus.READY_TO_PLAY).value(BigDecimal.TEN)
                        .schedule(Schedule.builder().startDateTime(LocalDateTime.now().plusHours(1)).build()).build()));

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = (Reservation) invocation.getArguments()[0];
            r.setId(1L);
            return r;
        });
        when(reservationMapper.map(any(Reservation.class))).thenAnswer(invocation ->
                ReservationDTO.builder().id(((Reservation) invocation.getArguments()[0]).getId())
                        .reservationStatus(((Reservation) invocation.getArguments()[0]).getReservationStatus().toString()).build());

        ReservationDTO reservationDTO = reservationService.cancelReservation(1L);
        assertEquals(1L, reservationDTO.getId());
        assertEquals(ReservationStatus.CANCELLED.toString(), reservationDTO.getReservationStatus());
    }

    @Test
    public void getRefundValueNoRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now();

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(BigDecimal.ZERO,
                reservationService.getRefundValue(Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()));
    }

    @Test
    public void getRefundValueQuarterRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(1);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(new BigDecimal(10).multiply(BigDecimal.valueOf(.25d)),
                reservationService.getRefundValue(Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()));
    }

    @Test
    public void getRefundValueHalfRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(11);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(new BigDecimal(10).multiply(BigDecimal.valueOf(.5d)),
                reservationService.getRefundValue(Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()));
    }

    @Test
    public void getRefundValueThreeQuartersRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(20);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(new BigDecimal(10).multiply(BigDecimal.valueOf(.75d)),
                reservationService.getRefundValue(Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()));
    }

    @Test
    public void getRefundValueFullRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusDays(2);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(new BigDecimal(10),
                reservationService.getRefundValue(Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()));
    }

    @Test
    public void rescheduleReservationPreviousNotFound() {
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.rescheduleReservation(1L, 1L));
    }

    @Test
    public void rescheduleReservationSameSchedule() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);

        Schedule oldSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        oldSchedule.setId(1L);
        Schedule newSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        newSchedule.setId(2L);

        Guest guest = Guest.builder().build();
        guest.setId(1L);

        Reservation oldReservation = Reservation.builder().schedule(oldSchedule)
                .guest(guest).reservationStatus(ReservationStatus.READY_TO_PLAY)
                .value(BigDecimal.TEN).build();
        oldReservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(oldReservation));

        assertThrows(IllegalArgumentException.class, () -> reservationService.rescheduleReservation(1L, 1L));
    }

    @Test
    public void rescheduleReservationScheduleNotFound() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);

        Schedule oldSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        oldSchedule.setId(1L);
        Schedule newSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        newSchedule.setId(2L);

        Guest guest = Guest.builder().build();
        guest.setId(1L);

        Reservation oldReservation = Reservation.builder().schedule(oldSchedule)
                .guest(guest).reservationStatus(ReservationStatus.READY_TO_PLAY)
                .value(BigDecimal.TEN).build();
        oldReservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(oldReservation));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.rescheduleReservation(1L, 2L));
    }

    @Test
    public void rescheduleReservationNotReadyToPlay() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);

        Schedule oldSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        oldSchedule.setId(1L);
        Schedule newSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        newSchedule.setId(2L);

        Guest guest = Guest.builder().build();
        guest.setId(1L);

        Reservation oldReservation = Reservation.builder().schedule(oldSchedule)
                .guest(guest).reservationStatus(ReservationStatus.CANCELLED)
                .value(BigDecimal.TEN).build();
        oldReservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(oldReservation));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(newSchedule));

        assertThrows(IllegalArgumentException.class, () -> reservationService.rescheduleReservation(1L, 2L));
    }

    @Test
    public void rescheduleReservationPreviousNotInFuture() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);

        Schedule oldSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        oldSchedule.setId(1L);
        Schedule newSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        newSchedule.setId(2L);

        Guest guest = Guest.builder().build();
        guest.setId(1L);

        Reservation oldReservation = Reservation.builder().schedule(oldSchedule)
                .guest(guest).reservationStatus(ReservationStatus.READY_TO_PLAY)
                .value(BigDecimal.TEN).build();
        oldReservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(oldReservation));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(newSchedule));

        assertThrows(IllegalArgumentException.class, () -> reservationService.rescheduleReservation(1L, 2L));
    }

    @Test
    public void rescheduleReservationGuestAlreadyReserved() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);

        Schedule oldSchedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(2))
                .endDateTime(LocalDateTime.now().plusHours(3)).tennisCourt(tennisCourt).build();
        oldSchedule.setId(1L);
        Schedule newSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        newSchedule.setId(2L);

        Guest guest = Guest.builder().build();
        guest.setId(1L);

        Reservation oldReservation = Reservation.builder().schedule(oldSchedule)
                .guest(guest).reservationStatus(ReservationStatus.READY_TO_PLAY)
                .value(BigDecimal.TEN).build();
        oldReservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(oldReservation));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(newSchedule));
        when(reservationRepository.findBySchedule_Id(anyLong())).thenReturn(Collections.singletonList(oldReservation));

        assertThrows(IllegalArgumentException.class, () -> reservationService.rescheduleReservation(1L, 2L));
    }

    @Test
    public void rescheduleReservationNewReservationNotInFuture() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);

        Schedule oldSchedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(2))
                .endDateTime(LocalDateTime.now().plusHours(3)).tennisCourt(tennisCourt).build();
        oldSchedule.setId(1L);
        Schedule newSchedule = Schedule.builder().startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(1)).tennisCourt(tennisCourt).build();
        newSchedule.setId(2L);

        Guest guest = Guest.builder().build();
        guest.setId(1L);

        Reservation oldReservation = Reservation.builder().schedule(oldSchedule)
                .guest(guest).reservationStatus(ReservationStatus.READY_TO_PLAY)
                .value(BigDecimal.TEN).build();
        oldReservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(oldReservation));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(newSchedule));
        when(reservationRepository.findBySchedule_Id(anyLong())).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> reservationService.rescheduleReservation(1L, 2L));
    }

    @Test
    public void rescheduleReservationSuccess() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);

        Schedule oldSchedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(2))
                .endDateTime(LocalDateTime.now().plusHours(3)).tennisCourt(tennisCourt).build();
        oldSchedule.setId(1L);
        Schedule newSchedule = Schedule.builder().startDateTime(LocalDateTime.now().plusHours(5))
                .endDateTime(LocalDateTime.now().plusHours(6)).tennisCourt(tennisCourt).build();
        newSchedule.setId(2L);

        Guest guest = Guest.builder().build();
        guest.setId(1L);

        Reservation oldReservation = Reservation.builder().schedule(oldSchedule)
                .guest(guest).reservationStatus(ReservationStatus.READY_TO_PLAY)
                .value(BigDecimal.TEN).build();
        oldReservation.setId(1L);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(oldReservation));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(newSchedule));
        when(reservationRepository.findBySchedule_Id(anyLong())).thenReturn(Collections.emptyList());
        when(reservationRepository.saveAll(anyIterable()))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        when(reservationMapper.map(any(Reservation.class))).thenAnswer(invocation -> {
            ReservationDTO reservationDTO = new ReservationDTO();
            Reservation reservation = (Reservation) invocation.getArguments()[0];
            reservationDTO.setId(reservation.getId());
            reservationDTO.setReservationStatus(reservation.getReservationStatus().toString());
            reservationDTO.setGuestId(reservation.getGuest().getId());
            reservationDTO.setScheduledId(reservation.getSchedule().getId());
            return reservationDTO;
        });

        ReservationDTO reservationDTO = reservationService.rescheduleReservation(1L, 2L);
        assertEquals(2L, reservationDTO.getScheduledId());
        assertEquals(ReservationStatus.RESCHEDULED.toString(), reservationDTO.getPreviousReservation().getReservationStatus());
        assertEquals(ReservationStatus.READY_TO_PLAY.toString(), reservationDTO.getReservationStatus());
        assertEquals(1L, reservationDTO.getPreviousReservation().getId());
    }
}