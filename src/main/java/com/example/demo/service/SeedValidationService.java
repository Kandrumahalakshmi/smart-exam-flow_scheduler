
package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SeedValidationService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final InvigilatorAvailabilityRepository invigilatorAvailabilityRepository;
    private final AccommodationRepository accommodationRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private final ExamRequirementRepository examRequirementRepository;
    private final ScheduleAssignmentRepository scheduleAssignmentRepository;

    public SeedValidationService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            ExamRepository examRepository,
            RoomRepository roomRepository,
            TimeSlotRepository timeSlotRepository,
            InvigilatorRepository invigilatorRepository,
            RoomAvailabilityRepository roomAvailabilityRepository,
            InvigilatorAvailabilityRepository invigilatorAvailabilityRepository,
            AccommodationRepository accommodationRepository,
            RoomEquipmentRepository roomEquipmentRepository,
            ExamRequirementRepository examRequirementRepository,
            ScheduleAssignmentRepository scheduleAssignmentRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.invigilatorRepository = invigilatorRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.invigilatorAvailabilityRepository = invigilatorAvailabilityRepository;
        this.accommodationRepository = accommodationRepository;
        this.roomEquipmentRepository = roomEquipmentRepository;
        this.examRequirementRepository = examRequirementRepository;
        this.scheduleAssignmentRepository = scheduleAssignmentRepository;
    }

    public Map<String, Object> validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        List<Student> students = studentRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        List<Exam> exams = examRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll();
        List<Invigilator> invigilators = invigilatorRepository.findAll();
        List<RoomAvailability> roomAvailabilities = roomAvailabilityRepository.findAll();
        List<InvigilatorAvailability> invigilatorAvailabilities =
                invigilatorAvailabilityRepository.findAll();
        List<Accommodation> accommodations = accommodationRepository.findAll();
        List<RoomEquipment> equipment = roomEquipmentRepository.findAll();
        List<ExamRequirement> requirements = examRequirementRepository.findAll();
        List<ScheduleAssignment> assignments = scheduleAssignmentRepository.findAll();

        // Required master data
        if (students.isEmpty()) errors.add("No students found.");
        if (courses.isEmpty()) errors.add("No courses found.");
        if (exams.isEmpty()) errors.add("No exams found.");
        if (rooms.isEmpty()) errors.add("No rooms found.");
        if (slots.isEmpty()) errors.add("No time slots found.");

        // Enrollments: valid references and unique student-course pairs
        Set<String> enrollmentPairs = new HashSet<>();
        for (Enrollment e : enrollments) {
            if (e.getStudent() == null || e.getCourse() == null) {
                errors.add("Enrollment " + e.getId() + " has a missing student or course.");
                continue;
            }

            String pair = e.getStudent().getId() + "|" + e.getCourse().getId();
            if (!enrollmentPairs.add(pair)) {
                errors.add("Duplicate enrollment pair: " + pair);
            }
        }

        // Exams
        Set<String> examIds = new HashSet<>();
        for (Exam e : exams) {
            if (e.getId() == null || !examIds.add(e.getId())) {
                errors.add("Exam has a missing or duplicate ID.");
            }
            if (e.getCourse() == null) {
                errors.add("Exam " + e.getId() + " has no course.");
            }
            if (e.getDurationMinutes() <= 0) {
                errors.add("Exam " + e.getId() + " has invalid duration.");
            }
            if (e.getRequiredCapacity() <= 0) {
                errors.add("Exam " + e.getId() + " has invalid required capacity.");
            }
            if (e.getRequiredInvigilators() <= 0) {
                errors.add("Exam " + e.getId() + " has invalid invigilator requirement.");
            }
            if (e.getStatus() == null) {
                errors.add("Exam " + e.getId() + " has no status.");
            }
        }

        // Rooms
        for (Room r : rooms) {
            if (r.getCapacity() <= 0) {
                errors.add("Room " + r.getId() + " has invalid capacity.");
            }
        }

        // Time slots
        for (TimeSlot slot : slots) {
            if (slot.getStartTime() == null || slot.getEndTime() == null
                    || !slot.getEndTime().isAfter(slot.getStartTime())) {
                errors.add("Time slot " + slot.getId() + " has invalid start/end times.");
            }
        }

        // Availability: uniqueness and valid references
        Set<String> roomSlotPairs = new HashSet<>();
        for (RoomAvailability a : roomAvailabilities) {
            if (a.getRoom() == null || a.getTimeSlot() == null) {
                errors.add("Room availability " + a.getId() + " has a missing reference.");
                continue;
            }
            String pair = a.getRoom().getId() + "|" + a.getTimeSlot().getId();
            if (!roomSlotPairs.add(pair)) {
                errors.add("Duplicate room availability: " + pair);
            }
        }

        Set<String> invigilatorSlotPairs = new HashSet<>();
        for (InvigilatorAvailability a : invigilatorAvailabilities) {
            if (a.getInvigilator() == null || a.getTimeSlot() == null) {
                errors.add("Invigilator availability " + a.getId() + " has a missing reference.");
                continue;
            }
            String pair = a.getInvigilator().getId() + "|" + a.getTimeSlot().getId();
            if (!invigilatorSlotPairs.add(pair)) {
                errors.add("Duplicate invigilator availability: " + pair);
            }
        }

        // Accommodations
        for (Accommodation a : accommodations) {
            if (a.getStudent() == null || a.getType() == null) {
                errors.add("Accommodation " + a.getId() + " has a missing student or type.");
            }
            if (a.getExtraTimeMinutes() < 0) {
                errors.add("Accommodation " + a.getId() + " has negative extra time.");
            }
        }

        // Equipment
        for (RoomEquipment e : equipment) {
            if (e.getRoom() == null || e.getEquipmentType() == null
                    || e.getEquipmentType().isBlank()) {
                errors.add("Room equipment " + e.getId() + " has missing room/type.");
            }
            if (e.getQuantity() <= 0) {
                errors.add("Room equipment " + e.getId() + " has invalid quantity.");
            }
        }

        // Exam requirements
        for (ExamRequirement r : requirements) {
            if (r.getExam() == null || r.getRequirementType() == null
                    || r.getRequirementType().isBlank()) {
                errors.add("Exam requirement " + r.getId() + " has missing exam/type.");
            }
        }

        // Existing assignments must be internally consistent
        for (ScheduleAssignment a : assignments) {
            if (a.getExam() == null || a.getStatus() == null) {
                errors.add("Assignment " + a.getId() + " has a missing exam or status.");
            }
            if (a.getStatus() == AssignmentStatus.ASSIGNED
                    && (a.getRoom() == null || a.getTimeSlot() == null)) {
                errors.add("Assigned record " + a.getId() + " has no room or time slot.");
            }
            if (a.getStatus() == AssignmentStatus.UNASSIGNED
                    && (a.getReasonCode() == null || a.getReasonCode().isBlank())) {
                errors.add("Unassigned record " + a.getId() + " has no reason code.");
            }
        }

        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("students", students.size());
        counts.put("courses", courses.size());
        counts.put("enrollments", enrollments.size());
        counts.put("exams", exams.size());
        counts.put("rooms", rooms.size());
        counts.put("timeSlots", slots.size());
        counts.put("invigilators", invigilators.size());
        counts.put("roomAvailabilities", roomAvailabilities.size());
        counts.put("invigilatorAvailabilities", invigilatorAvailabilities.size());
        counts.put("accommodations", accommodations.size());
        counts.put("roomEquipment", equipment.size());
        counts.put("examRequirements", requirements.size());
        counts.put("scheduleAssignments", assignments.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", errors.isEmpty());
        result.put("counts", counts);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        result.put("warnings", warnings);

        return result;
    }
}
