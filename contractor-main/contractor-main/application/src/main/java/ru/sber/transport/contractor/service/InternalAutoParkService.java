package ru.sber.transport.contractor.service;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.VehicleNormDto;
import ru.sber.transport.contractor.dto.enums.StaffSpeciality;
import ru.sber.transport.contractor.dto.internal.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for operations with internal auto-park
 */
public interface InternalAutoParkService {

    /**
     * Create staff in internal auto-park
     * @param createStaffDTO staff data
     * @param token authentication token
     * @param authorization authorization header
     */
    void createStaff(JwtAuthenticationToken token, CreateStaffDto createStaffDTO, String authorization);

    /**
     * Get staff from internal auto-park
     * @param getStaffDto staff data
     * @param token authentication token
     * @param authorization authorization header
     */
    Page<StaffDto> getStaff(JwtAuthenticationToken token, GetStaffDto getStaffDto, String authorization);

    /**
     * Delete staff from internal auto-park
     * @param token authentication token
     * @param speciality staff speciality
     * @param externalId staff id in internal auto-park
     * @param authorizationHeader authorization header
     */
    void deleteStaff(JwtAuthenticationToken token, GetStaffDto.Speciality speciality, UUID organizationId, UUID externalId, String authorizationHeader);

    /**
     * Create branch in internal auto-park
     * @param createBranchDto branch data
     * @param token authentication token
     * @param authorization authorization header
     */
    void createBranch(JwtAuthenticationToken token, CreateBranchDto createBranchDto, String authorization);

    /**
     * Update branch in internal auto-park
     * @param externalId branch id in internal auto-park
     * @param createBranchDto branch data
     * @param token authentication token
     * @param authorization authorization header
     */
    void updateBranch(UUID externalId, JwtAuthenticationToken token, CreateBranchDto createBranchDto, String authorization);

    /**
     * Delete branch in internal auto-park
     * @param externalId branch id in internal auto-park
     * @param token authentication token
     * @param organizationId organization id
     * @param authorization authorization header
     */
    void deleteBranch(UUID externalId, JwtAuthenticationToken token, UUID organizationId, String authorization);

    /**
     * Get branches from internal auto-park
     * @param getBranchDto branch data
     * @param token authentication token
     * @param authorization authorization header
     */
    Page<BranchResponseDto> getBranches(JwtAuthenticationToken token, GetBranchDto getBranchDto, String authorization);

    /**
     * Get vehicle norm from internal auto-park
     * @param organizationId organization id
     * @param authorization authorization header
     * @return vehicle norm
     */
    VehicleNormDto getVehicleNorm(UUID organizationId, String authorization);

    /**
     * Patch staff in internal auto-park
     * @param token authentication token
     * @param externalId external id
     * @param patchData patch data
     * @param speciality speciality
     * @param authorization authorization header
     */
    void patchStaff(JwtAuthenticationToken token, UUID externalId, List<PatchData> patchData, StaffSpeciality speciality, String authorization);
}
