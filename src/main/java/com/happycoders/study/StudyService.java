package com.happycoders.study;

import com.happycoders.domain.Account;
import com.happycoders.domain.Study;
import com.happycoders.domain.Tag;
import com.happycoders.domain.Zone;
import com.happycoders.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.happycoders.study.form.StudyForm.VALID_PATH_PATTERN;


@RequiredArgsConstructor
@Transactional
@Service
public class StudyService {

    private final StudyRepository studyRepository;

    private final ModelMapper modelMapper;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);
        checkIfManger(account, study);
        return study;
    }

    public Study getStudy(String path) {
        Study study = this.studyRepository.findByPath(path);
        checkIfExistStudy(study, path);
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        // 한 transaction 안에서 이루어지므로, 데이터가 변경된다. form 에 있는 데이터가 entity인 study로 modelmapper를 통해 변경!
        modelMapper.map(studyDescriptionForm, study);
    }

    public void updateStudyImage(Study study, String image) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.setImage(image));
    }

    public void enableStudyBanner(Study study) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.setUseBanner(true));
    }

    public void disableStudyBanner(Study study) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.setUseBanner(false));
    }

    public void addTag(Study study, Tag tag) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.getTags().add(tag));
    }

    public void removeTag(Study study, Tag tag) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.getTags().remove(tag));
    }

    public void addZone(Study study, Zone zone) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.getZones().add(zone));
    }

    public void removeZone(Study study, Zone zone) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.getZones().add(zone));
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkIfExistStudy(study, path);
        checkIfManger(account, study);
        return study;
    }

    public Study getStudyToUpdateZone (Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfExistStudy(study, path);
        checkIfManger(account, study);
        return study;
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistStudy(study, path);
        checkIfManger(account, study);
        return study;
    }

    private void checkIfManger(Account account, Study study) {
        if (!account.isManagerOf(study)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistStudy(Study study, String path) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void publish(Study study) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(Study::publish);
    }

    public void close(Study study) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(Study::close);
    }

    public void startRecruit(Study study) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(Study::startRecruit);
    }

    public void stopRecruit(Study study) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(Study::stopRecruit);
    }

    public void updateStudyPath(Study study, String newPath) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.updateNewPath(newPath));
    }

    public boolean isValidPath(String newPath) {
        if(!newPath.matches(VALID_PATH_PATTERN)) {
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void updateStudyTitle(Study study, String newTitle) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        byId.ifPresent(s -> s.setTitle(newTitle));
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void remove(Study study) {
        Optional<Study> byId = studyRepository.findById(study.getId());
        if (study.isRemovable()) {
           studyRepository.delete(byId.orElseThrow(() -> new RuntimeException("스터디가 없습니다.")));
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Study addMember (String path, Account account) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        study.getMembers().add(account);
        return study;
    }

    public Study removeMember (String path, Account account) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        study.getMembers().remove(account);
        return study;
    }

}








