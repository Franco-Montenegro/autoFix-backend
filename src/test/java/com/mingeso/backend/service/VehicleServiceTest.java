package com.mingeso.backend.service;

import com.mingeso.backend.dto.VehicleRequest;
import com.mingeso.backend.dto.VehicleResponse;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;
import com.mingeso.backend.exception.ConflictException;
import com.mingeso.backend.exception.ResourceNotFoundException;
import com.mingeso.backend.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void createSavesVehicleWithNormalizedData() {
        // El DTO normaliza patente, marca y modelo (trim + mayusculas) antes de llegar al servicio
        VehicleRequest request = new VehicleRequest(" abcd12 ", " toyota ", "corolla",
                VehicleType.SEDAN, 2020, EngineType.GASOLINE, 5);
        when(vehicleRepository.existsByLicensePlate("ABCD12")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        VehicleResponse response = vehicleService.create(request);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle saved = captor.getValue();
        assertThat(saved.getLicensePlate()).isEqualTo("ABCD12");
        assertThat(saved.getBrand()).isEqualTo("TOYOTA");
        assertThat(saved.getModel()).isEqualTo("COROLLA");
        assertThat(saved.getVehicleType()).isEqualTo(VehicleType.SEDAN);
        assertThat(saved.getEngineType()).isEqualTo(EngineType.GASOLINE);
        assertThat(saved.getManufactureYear()).isEqualTo(2020);
        assertThat(saved.getSeats()).isEqualTo(5);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.licensePlate()).isEqualTo("ABCD12");
        assertThat(response.brand()).isEqualTo("TOYOTA");
    }

    @Test
    void createRejectsDuplicateLicensePlate() {
        VehicleRequest request = new VehicleRequest("ABCD12", "TOYOTA", "COROLLA",
                VehicleType.SEDAN, 2020, EngineType.GASOLINE, 5);
        when(vehicleRepository.existsByLicensePlate("ABCD12")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ya existe un vehículo registrado con la patente ABCD12");
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void findAllReturnsVehiclesSortedByLicensePlate() {
        Vehicle first = TestData.vehicle("AAAA11", "FORD", VehicleType.PICKUP, EngineType.DIESEL, 2018);
        Vehicle second = TestData.toyotaSedan();
        when(vehicleRepository.findAll(Sort.by("licensePlate"))).thenReturn(List.of(first, second));

        List<VehicleResponse> responses = vehicleService.findAll();

        assertThat(responses).extracting(VehicleResponse::licensePlate).containsExactly("AAAA11", "ABCD12");
    }

    @Test
    void findByLicensePlateNormalizesThePlate() {
        when(vehicleRepository.findByLicensePlate("ABCD12")).thenReturn(Optional.of(TestData.toyotaSedan()));

        VehicleResponse response = vehicleService.findByLicensePlate(" abcd12 ");

        assertThat(response.licensePlate()).isEqualTo("ABCD12");
        assertThat(response.model()).isEqualTo("COROLLA");
    }

    @Test
    void findByLicensePlateFailsWhenVehicleDoesNotExist() {
        when(vehicleRepository.findByLicensePlate("ZZZZ99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.findByLicensePlate("zzzz99"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No existe un vehículo con la patente ZZZZ99");
    }
}
