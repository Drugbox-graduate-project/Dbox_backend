package com.drugbox.service;

import com.drugbox.common.exception.CustomException;
import com.drugbox.common.exception.ErrorCode;
import com.drugbox.domain.*;
import com.drugbox.dto.request.DrugDetailSaveRequest;
import com.drugbox.dto.request.DrugSaveRequest;
import com.drugbox.dto.request.DrugUpdateRequest;
import com.drugbox.dto.response.DisposalResponse;
import com.drugbox.dto.response.DrugResponse;
import com.drugbox.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DrugService {
    private final DrugRepository drugRepository;
    private final DrugboxRepository drugboxRepository;
    private final DrugInfoRepository drugInfoRepository;
    private final UserRepository userRepository;
    private final UserDrugboxRepository userDrugboxRepository;

    private final DrugApiService drugApiService;
    private final NotificationService notificationService;


    // 의약품 추가하기
    public List<Long> addDrugs(DrugSaveRequest request, Long userId) throws IOException, ParseException {
        Drugbox drugbox = getDrugboxOrThrow(request.getDrugboxId());
        List<Long> ids = new ArrayList<>();
        User user = getUserOrThrow(userId);
        for(int i=0; i<request.getDetail().size(); i++){
            DrugDetailSaveRequest detail = request.getDetail().get(i);
            if(detail.getCount() <= 0){
                throw new CustomException(ErrorCode.DRUG_COUNT_SHOULD_BE_ABOVE_ZERO);
            }
            Drug drug = Drug.builder()
                    .name(request.getName())
                    .type(request.getType())
                    .count(detail.getCount())
                    .location(detail.getLocation())
                    .expDate(detail.getExpDate())
                    .drugbox(drugbox)
                    .build();
            drugRepository.save(drug);
            saveDrugInfoIfEmpty(request.getName());

            ids.add(drug.getId());
            sendNotificationToAllOtherDrugboxMember(user, drugbox, "약 추가 알림",
                    user.getNickname()+"님이 "+drugbox.getName()+"에 "+drug.getName()+" "
                            +detail.getCount()+"개를 추가했습니다.");
        }
        return ids;
    }

    // 의약품 리스트 확인하기
    public List<DrugResponse> getDrugs(Long drugboxId){
        getDrugboxOrThrow(drugboxId);
        List<Drug> drugs = drugRepository.findAllByDrugboxId(drugboxId);
        return drugs.stream()
                .map(drug -> getDrugOrThrow(drug))
                .filter(drug -> !drug.isInDisposalList())
                .map(drug-> DrugToDrugResponse(drug))
                .collect(Collectors.toList());
    }

    // 의약품 사용하기
    public void useDrugs(List<DrugUpdateRequest> drugUpdateRequests){
        for(DrugUpdateRequest updateRequest : drugUpdateRequests){
            getDrugboxOrThrow(updateRequest.getDrugboxId());
            for(Long drugId : updateRequest.getDrugIds()){
                Drug drug = getDrugOrThrowById(drugId);
                int count = drug.getCount()-1;

                if(count==0){ // count 0되면 삭제
                    drugRepository.delete(drug);
                }else {
                    drug.setCount(count);
                    drugRepository.save(drug);
                }
            }
        }
    }

    // 의약품 폐기리스트로 옮기기
    public void disposeDrug(Long drugboxId, Long drugId){
        getDrugboxOrThrow(drugboxId);
        Drug drug = getDrugOrThrowById(drugId);

        drug.addToDisposalList();
        drugRepository.save(drug);
    }

    // 폐의약품 리스트에서 약 삭제하기
    public void deleteDrugFromDisposalList(List<DrugUpdateRequest> drugUpdateRequests){
        for(DrugUpdateRequest updateRequest : drugUpdateRequests){
            getDrugboxOrThrow(updateRequest.getDrugboxId());
            for(Long drugId : updateRequest.getDrugIds()){
                Drug drug = getDrugOrThrowById(drugId);
                if(!drug.isInDisposalList()){
                    throw new CustomException(ErrorCode.DRUG_NOT_IN_DISPOSAL_LIST);
                }
                drugRepository.delete(drug);
            }
        }
    }

    // 폐의약품 리스트 가져오기
    public List<DisposalResponse> getDisposalList(Long userId){
        getUserOrThrow(userId);
        List<Long> drugboxIds = userDrugboxRepository.findDrugboxIdByUserId(userId);
        List<DisposalResponse> disposalResponses = new ArrayList<>();
        for(Long id : drugboxIds){
            Drugbox drugbox = getDrugboxOrThrow(id);
            List<Drug> drugs = drugRepository.findAllByDrugboxId(id);
            List<DrugResponse> drugResponses = drugs.stream()
                    .map(drug -> getDrugOrThrow(drug))
                    .filter(drug -> drug.isInDisposalList())
                    .map(drug-> DrugToDrugResponse(drug))
                    .collect(Collectors.toList());

            DisposalResponse disposalResponse = DisposalResponse.builder()
                    .drugboxId(id)
                    .drugboxName(drugbox.getName())
                    .drugResponses(drugResponses)
                    .build();
            disposalResponses.add(disposalResponse);
        }
        return disposalResponses;
    }

    private Drugbox getDrugboxOrThrow(Long drugboxId) {
        return drugboxRepository.findById(drugboxId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DRUGBOX));
    }

    private Drug getDrugOrThrow(Drug drug){
        Long drugId = drug.getId();
        return drugRepository.findById(drugId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DRUG));
    }

    private Drug getDrugOrThrowById(Long drugId){
        return drugRepository.findById(drugId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DRUG));
    }

    private DrugInfo getDrugInfoOrThrow(String name){
        return drugInfoRepository.findByName(name)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DRUGINFO));
    }

    private void saveDrugInfoIfEmpty(String name) throws IOException, ParseException {
        Optional<DrugInfo> drugInfo = drugInfoRepository.findByName(name);
        if(drugInfo.isEmpty()){
            drugApiService.getDrugInfo(name);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public DrugResponse DrugToDrugResponse(Drug drug){
        return DrugResponse.builder()
                .id(drug.getId())
                .name(drug.getName())
                .location(drug.getLocation())
                .expDate(drug.getExpDate())
                .count(drug.getCount())
                .isInDisposalList(drug.isInDisposalList())
                .build();
    }

    private void sendNotificationToAllDrugboxMember(Drugbox drugbox, String title, String message){
        List<UserDrugbox> userDrugboxes = drugbox.getUserDrugboxes();
        for(UserDrugbox ud: userDrugboxes) {
            sendNotification(ud.getUser(), title, message);
        }
    }

    private void sendNotificationToAllOtherDrugboxMember(User user, Drugbox drugbox, String title, String message){
        List<UserDrugbox> userDrugboxes = drugbox.getUserDrugboxes();
        for(UserDrugbox ud: userDrugboxes) {
            if(ud.getUser() == user)
                continue;
            sendNotification(ud.getUser(), title, message);
        }
    }

    private void sendNotification(User user, String title, String message){
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();
        notificationService.makeNotification(notification);
    }
}
