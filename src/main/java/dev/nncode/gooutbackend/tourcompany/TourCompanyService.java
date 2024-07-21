package dev.nncode.gooutbackend.tourcompany;

import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
import dev.nncode.gooutbackend.tourcompany.model.TourCompanyDto;

public interface TourCompanyService {

    TourCompany registerTourCompany(TourCompanyDto payload);

    TourCompany approvedTourCompany(Integer id);


}
