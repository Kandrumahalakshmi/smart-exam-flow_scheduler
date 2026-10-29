
package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
public class SchedulingService {

    private final ExamRepository examRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final InvigilatorAvailabilityRepository invigilatorAvailabilityRepository;
    private final ScheduleAssignmentRepository assignmentRepository;
    private final AccommodationRepository accommodationRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private final ExamRequirementRepository examRequirementRepository;

    public SchedulingService(
            ExamRepository examRepository,
            EnrollmentRepository enrollmentRepository,
            RoomRepository roomRepository,
            TimeSlotRepository timeSlotRepository,
            InvigilatorRepository invigilatorRepository,
            RoomAvailabilityRepository roomAvailabilityRepository,
            InvigilatorAvailabilityRepository invigilatorAvailabilityRepository,
            ScheduleAssignmentRepository assignmentRepository,
            AccommodationRepository accommodationRepository,
            RoomEquipmentRepository roomEquipmentRepository,
            ExamRequirementRepository examRequirementRepository) {

        this.examRepository = examRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.invigilatorRepository = invigilatorRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.invigilatorAvailabilityRepository =
                invigilatorAvailabilityRepository;
        this.assignmentRepository = assignmentRepository;
        this.accommodationRepository = accommodationRepository;
        this.roomEquipmentRepository = roomEquipmentRepository;
        this.examRequirementRepository = examRequirementRepository;
    }

    @Transactional
    public List<ScheduleAssignment> generateSchedule() {

        if (assignmentRepository.count() > 0) {
            throw new IllegalStateException(
                    "Assignments already exist. Do not overwrite scheduling history.");
        }

        List<Exam> exams = new ArrayList<>(examRepository.findAll());
        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll();
        List<Invigilator> invigilators = invigilatorRepository.findAll();

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        List<RoomAvailability> roomAvailability =
                roomAvailabilityRepository.findAll();
        List<InvigilatorAvailability> invigilatorAvailability =
                invigilatorAvailabilityRepository.findAll();
        List<Accommodation> accommodations = accommodationRepository.findAll();
        List<RoomEquipment> roomEquipment = roomEquipmentRepository.findAll();
        List<ExamRequirement> examRequirements = examRequirementRepository.findAll();

        Map<String, Set<String>> studentsByCourse = new HashMap<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                studentsByCourse
                        .computeIfAbsent(
                                enrollment.getCourse().getId(),
                                key -> new HashSet<>())
                        .add(enrollment.getStudent().getId());
            }
        }

        // Deterministic ordering: larger exams first, then exam ID.
        exams.sort(
                Comparator.comparingInt(Exam::getRequiredCapacity)
                        .reversed()
                        .thenComparing(Exam::getId));

        rooms.sort(Comparator.comparing(Room::getId));
        slots.sort(
                Comparator.comparing(TimeSlot::getStartTime)
                        .thenComparing(TimeSlot::getId));
        invigilators.sort(Comparator.comparing(Invigilator::getId));

        List<ScheduleAssignment> created = new ArrayList<>();
        List<ScheduleAssignment> accepted = new ArrayList<>();

        for (Exam exam : exams) {

            if (exam.getStatus() != ExamStatus.ACTIVE) {
                continue;
            }

            Set<String> examStudents = studentsByCourse.getOrDefault(
                    exam.getCourse().getId(),
                    Collections.emptySet());

            int neededCapacity = Math.max(
                    exam.getRequiredCapacity(),
                    examStudents.size());
            int extraTime = accommodations.stream()
                    .filter(Accommodation::isActive)
                    .filter(a -> a.getStudent() != null
                            && examStudents.contains(a.getStudent().getId())
                            && a.getType() == AccommodationType.EXTENDED_TIME)
                    .mapToInt(Accommodation::getExtraTimeMinutes)
                    .max().orElse(0);
            boolean needsAccessibleRoom = accommodations.stream()
                    .filter(Accommodation::isActive)
                    .filter(a -> a.getStudent() != null
                            && examStudents.contains(a.getStudent().getId()))
                    .anyMatch(a -> a.getType() == AccommodationType.ACCESSIBLE_ROOM);
            List<ExamRequirement> requirements = examRequirements.stream()
                    .filter(r -> r.getExam() != null && r.getExam().getId().equals(exam.getId()))
                    .toList();

            boolean slotWasSuitable = false;
            boolean roomWasSuitable = false;
            boolean studentConflictFound = false;
            boolean insufficientInvigilators = false;
            boolean roomConstraintRejected = false;
            boolean assigned = false;

            for (TimeSlot slot : slots) {

                if (!slot.isActive()) {
                    continue;
                }

                long slotMinutes = Duration.between(
                        slot.getStartTime(),
                        slot.getEndTime()).toMinutes();

                if (slotMinutes < exam.getDurationMinutes() + extraTime) {
                    continue;
                }

                slotWasSuitable = true;

                for (Room room : rooms) {

                    if (!room.isActive()
                            || room.getCapacity() < neededCapacity) {
                        continue;
                    }

                    if (needsAccessibleRoom && !room.isAccessible()) {
                        roomConstraintRejected = true;
                        continue;
                    }

                    if (!satisfiesEquipmentRequirements(room, requirements, roomEquipment)) {
                        roomConstraintRejected = true;
                        continue;
                    }

                    if (!isRoomAvailable(
                            room, slot, roomAvailability)) {
                        continue;
                    }

                    if (roomOverlaps(room, slot, accepted)) {
                        continue;
                    }

                    roomWasSuitable = true;

                    if (hasStudentConflict(
                            examStudents, slot, accepted, studentsByCourse)) {
                        studentConflictFound = true;
                        continue;
                    }

                    // Collect the required number of distinct,
                    // available invigilators who have no overlapping exam.
                    List<Invigilator> selectedInvigilators =
                            new ArrayList<>();

                    for (Invigilator invigilator : invigilators) {

                        if (!invigilator.isActive()) {
                            continue;
                        }

                        if (!isInvigilatorAvailable(
                                invigilator, slot, invigilatorAvailability)) {
                            continue;
                        }

                        if (invigilatorOverlaps(
                                invigilator, slot, accepted)) {
                            continue;
                        }

                        selectedInvigilators.add(invigilator);

                        if (selectedInvigilators.size()
                                == exam.getRequiredInvigilators()) {
                            break;
                        }
                    }

                    if (selectedInvigilators.size()
                            < exam.getRequiredInvigilators()) {
                        insufficientInvigilators = true;
                        continue;
                    }

                    ScheduleAssignment assignment =
                            new ScheduleAssignment();

                    assignment.setId(
                            "ASN-" + exam.getId() + "-" + room.getId());
                    assignment.setExam(exam);
                    assignment.setRoom(room);
                    assignment.setTimeSlot(slot);
                    assignment.setInvigilators(selectedInvigilators);
                    assignment.setStatus(AssignmentStatus.ASSIGNED);
                    assignment.setReasonCode(null);

                    created.add(assignment);
                    accepted.add(assignment);
                    assigned = true;
                    break;
                }

                if (assigned) {
                    break;
                }
            }

            if (!assigned) {
                String reason;

                if (!slotWasSuitable) {
                    reason = "NO_SUITABLE_TIME_SLOT";
                } else if (!roomWasSuitable) {
                    reason = "NO_AVAILABLE_ROOM_WITH_SUFFICIENT_CAPACITY";
                } else if (roomConstraintRejected) {
                    reason = "ROOM_CONSTRAINT_UNSATISFIED";
                } else if (studentConflictFound) {
                    reason = "STUDENT_CONFLICT";
                } else if (insufficientInvigilators) {
                    reason = "INSUFFICIENT_AVAILABLE_INVIGILATORS";
                } else {
                    reason = "NO_FEASIBLE_COMBINATION";
                }

                created.add(unassigned(exam, reason));
            }
        }

        return assignmentRepository.saveAll(created);
    }

    private ScheduleAssignment unassigned(
            Exam exam, String reason) {

        ScheduleAssignment assignment = new ScheduleAssignment();
        assignment.setId("ASN-" + exam.getId() + "-UNASSIGNED");
        assignment.setExam(exam);
        assignment.setStatus(AssignmentStatus.UNASSIGNED);
        assignment.setReasonCode(reason);

        return assignment;
    }

    private boolean satisfiesEquipmentRequirements(
            Room room, List<ExamRequirement> requirements,
            List<RoomEquipment> equipment) {
        for (ExamRequirement requirement : requirements) {
            String type = requirement.getRequirementType();
            if (type == null || !(type.equalsIgnoreCase("EQUIPMENT")
                    || type.equalsIgnoreCase("ROOM_EQUIPMENT"))) {
                continue;
            }
            boolean found = equipment.stream().anyMatch(item ->
                    item.getRoom() != null
                            && item.getRoom().getId().equals(room.getId())
                            && item.getEquipmentType().equalsIgnoreCase(requirement.getRequirementValue())
                            && item.getQuantity() > 0);
            if (!found) return false;
        }
        return true;
    }

    private boolean isRoomAvailable(
            Room room,
            TimeSlot slot,
            List<RoomAvailability> availability) {

        return availability.stream().anyMatch(a ->
                a.getRoom().getId().equals(room.getId())
                        && a.getTimeSlot().getId().equals(slot.getId())
                        && a.isAvailable());
    }

    private boolean isInvigilatorAvailable(
            Invigilator invigilator,
            TimeSlot slot,
            List<InvigilatorAvailability> availability) {

        return availability.stream().anyMatch(a ->
                a.getInvigilator().getId().equals(invigilator.getId())
                        && a.getTimeSlot().getId().equals(slot.getId())
                        && a.isAvailable());
    }

    private boolean roomOverlaps(
            Room room,
            TimeSlot slot,
            List<ScheduleAssignment> assignments) {

        return assignments.stream().anyMatch(a ->
                a.getStatus() == AssignmentStatus.ASSIGNED
                        && a.getRoom() != null
                        && a.getTimeSlot() != null
                        && a.getRoom().getId().equals(room.getId())
                        && overlaps(slot, a.getTimeSlot()));
    }

    private boolean invigilatorOverlaps(
            Invigilator invigilator,
            TimeSlot slot,
            List<ScheduleAssignment> assignments) {

        return assignments.stream().anyMatch(a ->
                a.getStatus() == AssignmentStatus.ASSIGNED
                        && a.getTimeSlot() != null
                        && overlaps(slot, a.getTimeSlot())
                        && a.getInvigilators().stream().anyMatch(
                        assignedInvigilator ->
                                assignedInvigilator.getId()
                                        .equals(invigilator.getId())));
    }

    private boolean hasStudentConflict(
            Set<String> examStudents,
            TimeSlot slot,
            List<ScheduleAssignment> assignments,
            Map<String, Set<String>> studentsByCourse) {

        for (ScheduleAssignment existing : assignments) {

            if (existing.getStatus() != AssignmentStatus.ASSIGNED
                    || existing.getTimeSlot() == null
                    || !overlaps(slot, existing.getTimeSlot())) {
                continue;
            }

            Set<String> otherStudents = studentsByCourse.getOrDefault(
                    existing.getExam().getCourse().getId(),
                    Collections.emptySet());

            for (String studentId : examStudents) {
                if (otherStudents.contains(studentId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean overlaps(TimeSlot first, TimeSlot second) {
        return first.getStartTime().isBefore(second.getEndTime())
                && second.getStartTime().isBefore(first.getEndTime());
    }
}
