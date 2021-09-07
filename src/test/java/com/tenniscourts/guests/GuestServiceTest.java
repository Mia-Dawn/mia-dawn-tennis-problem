package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = GuestService.class)
public class GuestServiceTest {
    @Mock
    GuestRepository guestRepository;

    @Mock
    GuestMapper guestMapper;

    @InjectMocks
    GuestService guestService;

    @Test
    public void findGuestByIdNotFound() {
        when(guestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> guestService.findGuestById(1L));
    }

    @Test
    public void findGuestByIdSuccess() {
        Guest guest = Guest.builder().name("Tennis Man").build();
        guest.setId(1L);

        when(guestRepository.findById(anyLong())).thenReturn(Optional.of(guest));
        when(guestMapper.map(any(Guest.class))).thenAnswer(invocation -> {
            Guest guestArgument = (Guest) invocation.getArguments()[0];
            return GuestDTO.builder().id(guestArgument.getId()).name(guestArgument.getName()).build();
        });

        GuestDTO guestDTO = guestService.findGuestById(1L);
        assertEquals(1L, guestDTO.getId());
        assertEquals("Tennis Man", guestDTO.getName());
    }

    @Test
    public void findGuestByNameNotFound() {
        when(guestRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> guestService.findGuestByName("Tennis Man"));
    }

    @Test
    public void findGuestByNameSuccess() {
        Guest guest = Guest.builder().name("Tennis Man").build();
        guest.setId(1L);

        when(guestRepository.findByName(anyString())).thenReturn(Optional.of(guest));
        when(guestMapper.map(any(Guest.class))).thenAnswer(invocation -> {
            Guest guestArgument = (Guest) invocation.getArguments()[0];
            return GuestDTO.builder().id(guestArgument.getId()).name(guestArgument.getName()).build();
        });

        GuestDTO guestDTO = guestService.findGuestByName("Tennis Man");
        assertEquals(1L, guestDTO.getId());
        assertEquals("Tennis Man", guestDTO.getName());
    }

    @Test
    public void deleteGuestNotFound() {
        when(guestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> guestService.deleteGuest(1L));
    }

    @Test
    public void deleteGuestSuccess() {
        Guest guest = Guest.builder().name("Tennis Man").build();
        guest.setId(1L);

        when(guestRepository.findById(anyLong())).thenReturn(Optional.of(guest));
        when(guestMapper.map(any(Guest.class))).thenAnswer(invocation -> {
            Guest guestArgument = (Guest) invocation.getArguments()[0];
            return GuestDTO.builder().id(guestArgument.getId()).name(guestArgument.getName()).build();
        });

        GuestDTO guestDTO = guestService.deleteGuest(1L);
        assertEquals(1L, guestDTO.getId());
        assertEquals("Tennis Man", guestDTO.getName());
    }

    @Test
    public void updateGuestNotFound() {
        when(guestRepository.existsById(anyLong())).thenReturn(false);

        GuestDTO guestDTO = GuestDTO.builder().id(1L).name("Tennis Man").build();

        assertThrows(EntityNotFoundException.class, () -> guestService.updateGuest(guestDTO));
    }

    @Test
    public void updateGuestSuccess() {
        GuestDTO guestDTO = GuestDTO.builder().id(1L).name("Tennis Dude").build();

        when(guestRepository.existsById(anyLong())).thenReturn(true);
        when(guestMapper.map(any(GuestDTO.class))).thenAnswer(invocation -> {
            GuestDTO guestDTOArgument = (GuestDTO) invocation.getArguments()[0];
            Guest guest = Guest.builder().name(guestDTOArgument.getName()).build();
            guest.setId(guestDTO.getId());
            return guest;
        });
        when(guestMapper.map(any(Guest.class))).thenAnswer(invocation -> {
            Guest guestArgument = (Guest) invocation.getArguments()[0];
            return GuestDTO.builder().id(guestArgument.getId()).name(guestArgument.getName()).build();
        });
        when(guestRepository.save(any(Guest.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        GuestDTO returnGuestDTO = guestService.updateGuest(guestDTO);
        assertEquals(1L, returnGuestDTO.getId());
        assertEquals("Tennis Dude", returnGuestDTO.getName());
    }
}
