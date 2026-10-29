
package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class SyntheticDataGeneratorService {

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
    private final ScheduleAssignmentRepository assignmentRepository;

    public SyntheticDataGeneratorService(
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
            ScheduleAssignmentRepository assignmentRepository
    ) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.invigilatorRepository = invigilatorRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.invigilatorAvailabilityRepository =
                invigilatorAvailabilityRepository;
        this.accommodationRepository = accommodationRepository;
        this.roomEquipmentRepository = roomEquipmentRepository;
        this.examRequirementRepository = examRequirementRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public Map<String, Object> generate(String profile) {
        SeedConfiguration.SeedProfile config =
                SeedConfiguration.getProfile(profile);

        ensureDatabaseIsEmpty();

        Random random = new Random(SeedConfiguration.DEFAULT_SEED);

        List<Student> students = createStudents(config.students());
        studentRepository.saveAll(students);

        List<Course> courses = createCourses(config.courses());
        courseRepository.saveAll(courses);

        List<Enrollment> enrollments =
                createEnrollments(students, courses, config.enrollments(), random);
        enrollmentRepository.saveAll(enrollments);

        List<Room> rooms = createRooms(config.rooms(), random);
        roomRepository.saveAll(rooms);

        List<TimeSlot> slots = createTimeSlots(config.timeSlots());
        timeSlotRepository.saveAll(slots);

        List<Invigilator> invigilators =
                createInvigilators(config.invigilators());
        invigilatorRepository.saveAll(invigilators);

        List<Exam> exams = createExams(
                courses, config.exams(), random
        );
        examRepository.saveAll(exams);

        List<RoomAvailability> roomAvailability =
                createRoomAvailability(rooms, slots);
        roomAvailabilityRepository.saveAll(roomAvailability);

        List<InvigilatorAvailability> invigilatorAvailability =
                createInvigilatorAvailability(invigilators, slots);
        invigilatorAvailabilityRepository.saveAll(invigilatorAvailability);

        List<Accommodation> accommodations =
                createAccommodations(students, config.accommodations());
        accommodationRepository.saveAll(accommodations);

        List<RoomEquipment> equipment =
                createRoomEquipment(rooms, random);
        roomEquipmentRepository.saveAll(equipment);

        List<ExamRequirement> requirements =
                createExamRequirements(exams, random);
        examRequirementRepository.saveAll(requirements);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profile", profile);
        result.put("seed", SeedConfiguration.DEFAULT_SEED);
        result.put("students", students.size());
        result.put("courses", courses.size());
        result.put("enrollments", enrollments.size());
        result.put("exams", exams.size());
        result.put("rooms", rooms.size());
        result.put("timeSlots", slots.size());
        result.put("invigilators", invigilators.size());
        result.put("roomAvailability", roomAvailability.size());
        result.put("invigilatorAvailability",
                invigilatorAvailability.size());
        result.put("accommodations", accommodations.size());
        result.put("roomEquipment", equipment.size());
        result.put("examRequirements", requirements.size());
        result.put("scheduleAssignments", 0);
        result.put("message", "Synthetic baseline generated successfully");

        return result;
    }

    private void ensureDatabaseIsEmpty() {
        if (studentRepository.count() > 0
                || courseRepository.count() > 0
                || enrollmentRepository.count() > 0
                || examRepository.count() > 0
                || roomRepository.count() > 0
                || timeSlotRepository.count() > 0
                || invigilatorRepository.count() > 0
                || roomAvailabilityRepository.count() > 0
                || invigilatorAvailabilityRepository.count() > 0
                || accommodationRepository.count() > 0
                || roomEquipmentRepository.count() > 0
                || examRequirementRepository.count() > 0
                || assignmentRepository.count() > 0) {
            throw new IllegalStateException(
                    "Database is not empty. Use a fresh seed database."
            );
        }
    }

    private List<Student> createStudents(int count) {
        List<Student> result = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Student student = new Student();
            student.setId(String.format("STU-%06d", i));
            student.setName(String.format("Student %06d", i));
            student.setActive(true);
            result.add(student);
        }

        return result;
    }

    private List<Course> createCourses(int count) {
        List<Course> result = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Course course = new Course();
            course.setId(String.format("CRS-%03d", i));
            course.setName(String.format("Course %03d", i));
            course.setActive(true);
            result.add(course);
        }

        return result;
    }

    private List<Enrollment> createEnrollments(
            List<Student> students,
            List<Course> courses,
            int count,
            Random random
    ) {
        if (count > students.size() * courses.size()) {
            throw new IllegalArgumentException(
                    "Enrollment count exceeds unique student-course pairs."
            );
        }

        List<Enrollment> result = new ArrayList<>();
        Set<String> pairs = new HashSet<>();

        while (result.size() < count) {
            Student student = students.get(random.nextInt(students.size()));
            Course course = courses.get(random.nextInt(courses.size()));

            String pair = student.getId() + "|" + course.getId();

            if (!pairs.add(pair)) {
                continue;
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setId(String.format("ENR-%06d", result.size() + 1));
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setEnrolledOn(LocalDate.of(2026, 8, 1));
            enrollment.setStatus(EnrollmentStatus.ACTIVE);

            result.add(enrollment);
        }

        return result;
    }

    private List<Room> createRooms(int count, Random random) {
        List<Room> result = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Room room = new Room();
            room.setId(String.format("ROOM-%06d", i));
            room.setName(String.format("Exam Room %03d", i));
            room.setCapacity(150 + random.nextInt(351));
            room.setActive(true);
            room.setAccessible(i % 10 == 0);
            result.add(room);
        }

        return result;
    }

    private List<TimeSlot> createTimeSlots(int count) {
        List<TimeSlot> result = new ArrayList<>();

        LocalDate firstDate = LocalDate.of(2026, 11, 2);

        for (int i = 0; i < count; i++) {
            LocalDate date = firstDate.plusDays(i / 2);
            boolean morning = i % 2 == 0;

            LocalTime start = morning
                    ? LocalTime.of(9, 0)
                    : LocalTime.of(13, 0);

            TimeSlot slot = new TimeSlot();
            slot.setId(String.format("SLOT-%06d", i + 1));
            slot.setStartTime(LocalDateTime.of(date, start));
            slot.setEndTime(LocalDateTime.of(date, start.plusHours(3)));
            slot.setActive(true);

            result.add(slot);
        }

        return result;
    }

    private List<Invigilator> createInvigilators(int count) {
        List<Invigilator> result = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Invigilator invigilator = new Invigilator();
            invigilator.setId(String.format("INV-%06d", i));
            invigilator.setName(String.format("Invigilator %03d", i));
            invigilator.setActive(true);
            result.add(invigilator);
        }

        return result;
    }

    private List<Exam> createExams(
            List<Course> courses,
            int count,
            Random random
    ) {
        List<Exam> result = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Exam exam = new Exam();
            exam.setId(String.format("EXAM-2026-%06d", i));
            exam.setCourse(courses.get((i - 1) % courses.size()));
            exam.setDurationMinutes(60 + random.nextInt(121));
            exam.setRequiredCapacity(50 + random.nextInt(201));
            exam.setRequiredInvigilators(1 + random.nextInt(3));
            exam.setStatus(ExamStatus.ACTIVE);
            result.add(exam);
        }

        return result;
    }

    private List<RoomAvailability> createRoomAvailability(
            List<Room> rooms,
            List<TimeSlot> slots
    ) {
        List<RoomAvailability> result = new ArrayList<>();
        int sequence = 1;

        for (Room room : rooms) {
            for (TimeSlot slot : slots) {
                RoomAvailability availability = new RoomAvailability();
                availability.setId(String.format("RAV-%06d", sequence++));
                availability.setRoom(room);
                availability.setTimeSlot(slot);
                availability.setAvailable(true);
                result.add(availability);
            }
        }

        return result;
    }

    private List<InvigilatorAvailability> createInvigilatorAvailability(
            List<Invigilator> invigilators,
            List<TimeSlot> slots
    ) {
        List<InvigilatorAvailability> result = new ArrayList<>();
        int sequence = 1;

        for (Invigilator invigilator : invigilators) {
            for (TimeSlot slot : slots) {
                InvigilatorAvailability availability =
                        new InvigilatorAvailability();

                availability.setId(String.format("IAV-%06d", sequence++));
                availability.setInvigilator(invigilator);
                availability.setTimeSlot(slot);
                availability.setAvailable(true);
                result.add(availability);
            }
        }

        return result;
    }

    private List<Accommodation> createAccommodations(
            List<Student> students,
            int count
    ) {
        if (count > students.size()) {
            throw new IllegalArgumentException(
                    "Accommodation count cannot exceed student count."
            );
        }

        List<Accommodation> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Accommodation accommodation = new Accommodation();
            accommodation.setId(String.format("ACC-%06d", i + 1));
            accommodation.setStudent(students.get(i));
            accommodation.setActive(true);

            if (i % 2 == 0) {
                accommodation.setType(AccommodationType.EXTENDED_TIME);
                accommodation.setExtraTimeMinutes(30);
                accommodation.setDetails("30 additional exam minutes");
            } else {
                accommodation.setType(AccommodationType.ACCESSIBLE_ROOM);
                accommodation.setExtraTimeMinutes(0);
                accommodation.setDetails("Accessible room required");
            }

            result.add(accommodation);
        }

        return result;
    }

    private List<RoomEquipment> createRoomEquipment(
            List<Room> rooms,
            Random random
    ) {
        List<RoomEquipment> result = new ArrayList<>();
        int sequence = 1;

        for (Room room : rooms) {
            for (String type : List.of("COMPUTER", "PROJECTOR")) {
                RoomEquipment equipment = new RoomEquipment();
                equipment.setId(String.format("REQE-%06d", sequence++));
                equipment.setRoom(room);
                equipment.setEquipmentType(type);
                equipment.setQuantity(1 + random.nextInt(20));
                result.add(equipment);
            }
        }

        return result;
    }

    private List<ExamRequirement> createExamRequirements(
            List<Exam> exams,
            Random random
    ) {
        List<ExamRequirement> result = new ArrayList<>();
        int sequence = 1;

        for (Exam exam : exams) {
            ExamRequirement requirement = new ExamRequirement();
            requirement.setId(String.format("REQ-%06d", sequence++));
            requirement.setExam(exam);

            String equipment = random.nextBoolean()
                    ? "COMPUTER"
                    : "PROJECTOR";

            requirement.setRequirementType("EQUIPMENT");
            requirement.setRequirementValue(equipment);
            requirement.setDescription(
                    "Requires room equipment: " + equipment
            );

            result.add(requirement);
        }

        return result;
    }
}
