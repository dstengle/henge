package com.kenzan.henge.resource.v1;

import static com.kenzan.henge.domain.utils.ScopeUtils.parseScopeString;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.Group;
import com.kenzan.henge.domain.model.MappingGroup;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.validator.CheckGroup;
import com.kenzan.henge.service.PropertyGroupBD;
import com.kenzan.henge.service.VersionSetBD;
import com.kenzan.henge.service.VersionSetMappingBD;

import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/v1/convenience")
@Api("v1 - convenience")
public class ConvenienceRS {

    private VersionSetBD versionSetBD;

    private PropertyGroupBD propertyGroupBD;

    private VersionSetMappingBD mappingBD;

    @Autowired
    public ConvenienceRS(final VersionSetBD versionSetBD, final PropertyGroupBD propertyGroupBD,
        final VersionSetMappingBD mappingBD) {

        this.versionSetBD = versionSetBD;
        this.propertyGroupBD = propertyGroupBD;
        this.mappingBD = mappingBD;
    }

    @ApiOperation(value = "Creates PropertyGroup, VersionSet and MappingGroup")
    @ApiImplicitParams({ @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue = "application/json",
                                           required = true, dataType = "string", paramType = "header") })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS"),
        @ApiResponse(code = 400, message = "INVALID REQUEST"), @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
        @ApiResponse(code = 403, message = "UNAUTHORIZED"), @ApiResponse(code = 409, message = "CONFLICT"),
        @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR") })
    @Path("/batch")
    @POST
    public Response
        batchInsert(@Valid @CheckGroup @ApiParam(value = "Request body with PropertyGroup, VersionSet and MappingGroup",
                                                 required = true) final Group group) {

        final Set<VersionSet> versionSetList = group.getVersionSetList();
        final Set<PropertyGroup> propertyGroupList = group.getPropertyGroupList();
        final Set<MappingGroup> mappingList = group.getMappingList();

        if (isNotEmpty(propertyGroupList)) {
            propertyGroupList
                .parallelStream()
                .filter(
                    p -> {
                        Optional<PropertyGroup> pg = propertyGroupBD.read(p.getName(), p.getVersion());
                        if (!pg.isPresent()) {
                            return true;
                        }
                        if (!pg.get().equals(p)) {
                            throw new ValidationException(
                                "The batch contains a PropertyGroup (name: "
                                    + p.getName()
                                    + ", version: "
                                    + p.getVersion()
                                    + ") that already exists in the repository. Normally the insertion would be ignored, but the PropertyGroups contain different data.");
                        }
                        return false;
                        
                    }).forEach(p -> propertyGroupBD.create(p));
        }

        if (isNotEmpty(versionSetList)) {
            versionSetList
                .parallelStream()
                .filter(
                    v -> {
                        Optional<VersionSet> vs = versionSetBD.read(v.getName(), v.getVersion());
                        if (!vs.isPresent()) {
                            return true;
                        }
                        if (!vs.get().equals(v)) {
                            throw new ValidationException(
                                "The batch contains a VersionSet (name: "
                                    + v.getName()
                                    + ", version: "
                                    + v.getVersion()
                                    + ") that already exists in the repository. Normally the insertion would be ignored, but the VersionSets contain different data.");
                        }
                        return false;

                    }).forEach(v -> versionSetBD.create(v));
        }

        if (isNotEmpty(mappingList)) {
            mappingList.parallelStream().forEach(
                m -> mappingBD.setMapping(Optional.ofNullable(m.getApplication()), transform(m.getScopeString()),
                    m.getVsReference()));
        }

        return Response.ok().build();
    }

    private Set<Scope> transform(final String scopeString) {

        return isNotBlank(scopeString) ? parseScopeString(scopeString) : Sets.newHashSet();
    }

}
