package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.reservations.ReservationService;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = ScheduleService.class)
public class ScheduleServiceTest {
    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    TennisCourtRepository tennisCourtRepository;

    @Mock
    ScheduleMapper scheduleMapper;

    @InjectMocks
    ScheduleService scheduleService;

    @Test
    public void addScheduleInPast() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.now().minusHours(1));
        createScheduleRequestDTO.setTennisCourtId(1L);

        assertThrows(IllegalArgumentException.class, () -> scheduleService.addSchedule(1L, createScheduleRequestDTO));
    }

    @Test
    public void addScheduleOverlapping() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.now().plusHours(1));
        createScheduleRequestDTO.setTennisCourtId(1L);

        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);
        tennisCourt.setName("Best Court");

        Schedule existingSchedule = new Schedule();
        existingSchedule.setId(1L);
        existingSchedule.setTennisCourt(tennisCourt);
        existingSchedule.setStartDateTime(LocalDateTime.now());
        existingSchedule.setEndDateTime(LocalDateTime.now().plusHours(2));

        when(scheduleMapper.map(any(List.class))).thenAnswer(invocation -> {
            Schedule s = (Schedule) (((List) invocation.getArguments()[0]).get(0));
            return Collections.singletonList(ScheduleDTO.builder().id(s.getId()).tennisCourtId(s.getTennisCourt().getId())
                    .startDateTime(s.getStartDateTime()).endDateTime(s.getEndDateTime()).build());
        });
        when(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(anyLong())).thenReturn(Collections.singletonList(existingSchedule));

        assertThrows(IllegalArgumentException.class, () -> scheduleService.addSchedule(1L, createScheduleRequestDTO));
    }

    @Test
    public void addScheduleCourtNotFound() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.now().plusHours(1));
        createScheduleRequestDTO.setTennisCourtId(1L);

        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);
        tennisCourt.setName("Best Court");

        Schedule existingSchedule = new Schedule();
        existingSchedule.setId(1L);
        existingSchedule.setTennisCourt(tennisCourt);
        existingSchedule.setStartDateTime(LocalDateTime.now().plusHours(3));
        existingSchedule.setEndDateTime(LocalDateTime.now().plusHours(4));

        when(scheduleMapper.map(any(List.class))).thenAnswer(invocation -> {
            Schedule s = (Schedule) (((List) invocation.getArguments()[0]).get(0));
            return Collections.singletonList(ScheduleDTO.builder().id(s.getId()).tennisCourtId(s.getTennisCourt().getId())
                    .startDateTime(s.getStartDateTime()).endDateTime(s.getEndDateTime()).build());
        });
        when(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(anyLong())).thenReturn(Collections.singletonList(existingSchedule));
        when(tennisCourtRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> scheduleService.addSchedule(1L, createScheduleRequestDTO));
    }

    @Test
    public void addScheduleSuccess() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.now().plusHours(1));
        createScheduleRequestDTO.setTennisCourtId(1L);

        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);
        tennisCourt.setName("Best Court");

        Schedule existingSchedule = new Schedule();
        existingSchedule.setId(1L);
        existingSchedule.setTennisCourt(tennisCourt);
        existingSchedule.setStartDateTime(LocalDateTime.now().plusHours(3));
        existingSchedule.setEndDateTime(LocalDateTime.now().plusHours(4));

        when(scheduleMapper.map(any(List.class))).thenAnswer(invocation -> {
            Schedule s = (Schedule) (((List) invocation.getArguments()[0]).get(0));
            return Collections.singletonList(ScheduleDTO.builder().id(s.getId()).tennisCourtId(s.getTennisCourt().getId())
                    .startDateTime(s.getStartDateTime()).endDateTime(s.getEndDateTime()).build());
        });
        when(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(anyLong())).thenReturn(Collections.singletonList(existingSchedule));
        when(tennisCourtRepository.findById(anyLong())).thenReturn(Optional.of(tennisCourt));
        when(scheduleRepository.saveAndFlush(any(Schedule.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(scheduleMapper.map(any(Schedule.class))).thenAnswer(invocation -> {
            Schedule s = (Schedule) invocation.getArguments()[0];
            return ScheduleDTO.builder().id(s.getId()).tennisCourtId(s.getTennisCourt().getId())
                    .startDateTime(s.getStartDateTime()).endDateTime(s.getEndDateTime()).build();
        });

        ScheduleDTO returnScheduleDTO = scheduleService.addSchedule(1L, createScheduleRequestDTO);
        assertEquals(1L, returnScheduleDTO.getTennisCourtId());
        assertTrue(returnScheduleDTO.getStartDateTime().isAfter(LocalDateTime.now()));
    }

    @Test
    public void findScheduleNotFound() {
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> scheduleService.findSchedule(1L));
    }

    @Test
    public void findScheduleSuccess() {
        TennisCourt tennisCourt = new TennisCourt();
        tennisCourt.setId(1L);
        tennisCourt.setName("Best Court");

        Schedule existingSchedule = new Schedule();
        existingSchedule.setId(1L);
        existingSchedule.setTennisCourt(tennisCourt);
        existingSchedule.setStartDateTime(LocalDateTime.now().plusHours(3));
        existingSchedule.setEndDateTime(LocalDateTime.now().plusHours(4));

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(existingSchedule));
        when(scheduleMapper.map(any(Schedule.class))).thenAnswer(invocation -> {
            Schedule s = (Schedule) invocation.getArguments()[0];
            return ScheduleDTO.builder().id(s.getId()).tennisCourtId(s.getTennisCourt().getId())
                    .startDateTime(s.getStartDateTime()).endDateTime(s.getEndDateTime()).build();
        });

        ScheduleDTO returnScheduleDTO = scheduleService.findSchedule(1L);
        assertEquals(1L, returnScheduleDTO.getTennisCourtId());
        assertTrue(returnScheduleDTO.getStartDateTime().isAfter(LocalDateTime.now()));
    }
}
