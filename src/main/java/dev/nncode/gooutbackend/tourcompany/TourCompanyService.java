package dev.nncode.gooutbackend.tourcompany;

import dev.nncode.gooutbackend.tourcompany.dto.RegisterTourCompanyDto;
import dev.nncode.gooutbackend.tourcompany.model.TourCompany;

public interface TourCompanyService {

    TourCompany registerTourCompany(RegisterTourCompanyDto payload);

    TourCompany approvedTourCompany(Integer id);


}
