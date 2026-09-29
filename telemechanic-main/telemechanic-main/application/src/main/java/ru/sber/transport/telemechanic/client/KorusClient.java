package ru.sber.transport.telemechanic.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.sber.transport.telemechanic.dto.ewb.*;

import java.util.UUID;


@FeignClient("korus")
public interface KorusClient {
    @PostMapping(value = "/v1/login")
    TokenDto auth(@RequestBody AuthRequest request);

    @PostMapping(value = "/v1/client/gis/uuid/external")
    ResponseEntity<UuidDto> createNewQr();

    @PostMapping("v1/doc/send")
    KorusEwbTitleResponse sendTitle(@RequestBody KorusTitleRequest korusTitleRequest);
    
    @GetMapping("v1/chain/{chainId}/docs")
    ChainDocsDto getChainDocs(@PathVariable("chainId") UUID chainId);
    
    @GetMapping("v1/doc/{id}/content")
    byte[] getDocArchive(@PathVariable("id") UUID docId);
}
