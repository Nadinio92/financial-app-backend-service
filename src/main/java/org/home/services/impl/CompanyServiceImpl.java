package org.home.services.impl;

import lombok.RequiredArgsConstructor;
import org.home.dto.CompanyCreateOrUpdateDto;
import org.home.dto.CompanyDto;
import org.home.models.*;
import org.home.repository.AnalystRepository;
import org.home.repository.CompanyAnalystRepository;
import org.home.repository.CompanyRepository;
import org.home.repository.SectorRepository;
import org.home.services.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor // Creates constructor with required ('final') parameters and set fields values
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final AnalystRepository analystRepository;
    private final SectorRepository sectorRepository;
    private final CompanyAnalystRepository companyAnalystRepository;

    @Transactional
    @Override
    public Long addCompany(CompanyCreateOrUpdateDto dto) {

        CompanyEntity companyEntity = new CompanyEntity();
        companyEntity.setName(dto.getName());
        companyEntity.setMarketCap(dto.getMarketCap());
        var sectorEntity = new SectorEntity();
        sectorEntity.setId(dto.getSector());
        companyEntity.setSector(sectorEntity);

        var savedCompanyEntity = companyRepository.save(companyEntity);

        List<CompanyAnalystEntity> companyAnalystEntityList = new ArrayList<>();

        for(Long analystId : dto.getAnalysts() ){
            CompanyAnalystEntity companyAnalystEntity = new CompanyAnalystEntity();
            CompanyAnalystId companyAnalystId = new CompanyAnalystId();
            companyAnalystId.setCompanyId(savedCompanyEntity.getId());
            companyAnalystId.setAnalystId(analystId);
            companyAnalystEntity.setId(companyAnalystId);

            companyAnalystEntity.setCompany(savedCompanyEntity);

            var analyst = new AnalystEntity();
            analyst.setId(analystId);
            companyAnalystEntity.setAnalyst(analyst);

            companyAnalystEntityList.add(companyAnalystEntity);
        }
        savedCompanyEntity.getCompanyAnalysts().addAll(companyAnalystEntityList);
        companyRepository.save(savedCompanyEntity);

        return savedCompanyEntity.getId();
    }

    @Transactional
    @Override
    public List<CompanyDto> getCompanies() {

        var companyDtoList = new ArrayList<CompanyDto>();

        for(CompanyEntity companyEntity : companyRepository.findAll()) { // N+1 problem
            var companyDto = new CompanyDto();
            companyDto.setId(companyEntity.getId());
            companyDto.setName(companyEntity.getName());
            companyDto.setMarketCap(companyEntity.getMarketCap());
            if(companyEntity.getSector() != null) {
                companyDto.setSector(companyEntity.getSector().getName());
            }

            List<String> analystLists = new ArrayList<>();

            for(CompanyAnalystEntity companyAnalystEntity : companyEntity.getCompanyAnalysts()){
                String analystName = companyAnalystEntity.getAnalyst().getName();
                analystLists.add(analystName);
            }
            companyDto.setAnalysts(analystLists);

            companyDtoList.add(companyDto);
        }
        return companyDtoList;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        companyAnalystRepository.deleteCompanyAnalystEntityByCompanyId(id);
        companyRepository.deleteById(id);
        return true;
    }
}
