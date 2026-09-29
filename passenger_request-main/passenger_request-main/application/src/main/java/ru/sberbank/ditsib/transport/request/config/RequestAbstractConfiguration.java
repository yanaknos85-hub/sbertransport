package ru.sberbank.ditsib.transport.request.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.service.impl.*;
import ru.sberbank.ditsib.transport.request.service.impl.search.*;
import ru.sberbank.ditsib.transport.request.service.publicTransport.impl.RequestForPublicServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RequestAbstractConfiguration {
    
    @Bean
    public List<AbstractRequestSearchSpecService<? extends Request>> requestSearchSpecServices() {
        var list = new ArrayList<AbstractRequestSearchSpecService<? extends Request>>();
        list.add(new RequestSearchSpecForPersonalImpl());
        list.add(new RequestSearchSpecForPublicImpl());
        list.add(new RequestSearchSpecForTaxiImpl());
        list.add(new RequestSearchSpecForCarsharingImpl());
        return list;
    }
    
    @Bean
    public List<AbstractTransportTypeService> transportTypeServices(
            RequestForTaxiServiceImpl taxiService,
            RequestForPublicServiceImpl publicService,
            RequestForPersonalServiceImpl personalService,
            RequestForCarsharingServiceImpl carsharingService,
            RequestForGroupTransferServiceImpl requestForGroupTransferService
                                                                   ) {
        var list = new ArrayList<AbstractTransportTypeService>();
        list.add(taxiService);
        list.add(publicService);
        list.add(personalService);
        list.add(carsharingService);
        list.add(requestForGroupTransferService);
        return list;
    }
    
}
