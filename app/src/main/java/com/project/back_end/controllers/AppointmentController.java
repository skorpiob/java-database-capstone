package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.AppService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;


import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {


// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


    // 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.
    private final AppointmentService appointmentService;
    private final AppService service;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;


    public AppointmentController(AppointmentService appointmentService, AppService service, AppointmentRepository appointmentRepository, DoctorRepository doctorRepository) {
        this.appointmentService = appointmentService;
        this.service = service;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }


    // 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable LocalDate date,
                                             @PathVariable String patientName,
                                             @PathVariable String token) {

        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation != null) {
            return tokenValidation;
        }
        Doctor doctor = doctorRepository.findByEmail(token);
        Long doctorId = doctor.getId();

//wywolanie getAppointments(Long doctorId, LocalDate date, String patientName)
        return ResponseEntity.ok(appointmentService.getAppointments(doctorId, date, patientName));
    }


    // 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@PathVariable String token,
                                                               @RequestBody Appointment appointment) {

        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation != null) {
            return tokenValidation;
        }

        int validation = service.validateAppointment(appointment);

        if (validation == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid doctor ID"));
        }

        if (validation == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Selected time slot is unavailable"));
        }

        appointmentService.bookAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Appointment booked successfully"));
    }


    // 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.
    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(@PathVariable String token,
                                               @RequestBody Appointment appointment) {

        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation != null) {
            return tokenValidation;
        }

        return ResponseEntity.ok(
                appointmentService.updateAppointment(
                        appointment.getId(),
                        appointment.getPatient().getId(),
                        appointment.getAppointmentTime()
                )
        );
    }


    // 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id,
                                               @PathVariable String token) {

        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation != null) {
            return tokenValidation;
        }

        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        long patientId = appointment.getPatient().getId();

        // cancelAppointment input - Long appointmentId, Long patientId
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, patientId));
    }

}