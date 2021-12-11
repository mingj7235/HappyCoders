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
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
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
        Study study = studyRepository.findAccountWithTagsByPath(path);
        checkIfExistStudy(study, path);
        checkIfManger(account, study);
        return study;
    }

    public Study getStudyToUpdateZone (Account account, String path) {
        Study study = studyRepository.findAccountWithZonesByPath(path);
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
        study.publish();
    }

    public void close(Study study) {
        study.close();
    }

    public void startRecruit(Study study) {
        study.startRecruit();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
    }

    public void updateStudyPath(Study study, String newPath) {
        study.updateNewPath(newPath);
    }

}








