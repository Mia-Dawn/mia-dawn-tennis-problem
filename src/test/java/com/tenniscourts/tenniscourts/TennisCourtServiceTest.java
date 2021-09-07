package com.tenniscourts.tenniscourts;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.GuestRepository;
import com.tenniscourts.reservations.ReservationMapper;
import com.tenniscourts.reservations.ReservationRepository;
import com.tenniscourts.reservations.ReservationService;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleRepository;
import com.tenniscourts.schedules.ScheduleService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = TennisCourtService.class)
public class TennisCourtServiceTest {
    @Mock
    TennisCourtRepository tennisCourtRepository;

    @Mock
    ScheduleService scheduleService;

    @Mock
    TennisCourtMapper tennisCourtMapper;

    @InjectMocks
    TennisCourtService tennisCourtService;

    @Test
    public void findTennisCourtByIdNotFound() {
        when(tennisCourtRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> tennisCourtService.findTennisCourtById(1L));
    }

    @Test
    public void findTennisCourtByIdSuccess() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);
        tennisCourt.setName("Best Court");

        when(tennisCourtRepository.findById(anyLong())).thenReturn(Optional.of(tennisCourt));
        when(tennisCourtMapper.map(tennisCourt)).thenAnswer(invocation -> {
            TennisCourt tennisCourtArgument = (TennisCourt) invocation.getArguments()[0];
            return TennisCourtDTO.builder().id(tennisCourtArgument.getId()).name(tennisCourtArgument.getName()).build();
        });

        TennisCourtDTO tennisCourtDTO = tennisCourtService.findTennisCourtById(1L);
        assertEquals(1L, tennisCourtDTO.getId());
        assertEquals("Best Court", tennisCourtDTO.getName());
        assertNull(tennisCourtDTO.getTennisCourtSchedules());
    }

    @Test
    public void findTennisCourtWithSchedulesByIdNotFound() {
        when(tennisCourtRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> tennisCourtService.findTennisCourtWithSchedulesById(1L));
    }

    @Test
    public void findTennisCourtWithSchedulesByIdSuccess() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);
        tennisCourt.setName("Best Court");

        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setId(1L);
        scheduleDTO.setStartDateTime(LocalDateTime.now());
        scheduleDTO.setEndDateTime(LocalDateTime.now().plusHours(1));
        scheduleDTO.setTennisCourtId(tennisCourt.getId());

        when(tennisCourtRepository.findById(anyLong())).thenReturn(Optional.of(tennisCourt));
        when(scheduleService.findSchedulesByTennisCourtId(anyLong())).thenReturn(Collections.singletonList(scheduleDTO));
        when(tennisCourtMapper.map(tennisCourt)).thenAnswer(invocation -> {
            TennisCourt tennisCourtArgument = (TennisCourt) invocation.getArguments()[0];
            return TennisCourtDTO.builder().id(tennisCourtArgument.getId()).name(tennisCourtArgument.getName()).build();
        });

        TennisCourtDTO tennisCourtDTO = tennisCourtService.findTennisCourtWithSchedulesById(1L);
        assertEquals(1L, tennisCourtDTO.getId());
        assertEquals("Best Court", tennisCourtDTO.getName());
        assertNotNull(tennisCourtDTO.getTennisCourtSchedules());
        assertFalse(tennisCourtDTO.getTennisCourtSchedules().isEmpty());
    }
}
