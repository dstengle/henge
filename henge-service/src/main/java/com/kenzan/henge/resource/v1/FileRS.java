package com.kenzan.henge.resource.v1;

import com.codahale.metrics.annotation.Timed;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.FileVersionReference;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeException;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.exception.RuntimeHengeException;
import com.kenzan.henge.service.FileBD;
import com.kenzan.henge.service.VersionSetBD;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Igor K. Shiohara
 *
 */
@Component
@Path("/v1/files")
@Api("v1 - files")
@Produces(MediaType.APPLICATION_JSON)
public class FileRS {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileRS.class);

	private FileBD fileBD;

    private JsonUtils jsonUtils;
    
    private VersionSetBD versionSetBD;


	@Autowired
	public FileRS(FileBD fileBD, JsonUtils jsonUtils, VersionSetBD versionSetBD) {
		this.fileBD = fileBD;

        this.jsonUtils = jsonUtils;
        this.versionSetBD = versionSetBD;
                        
	}

	@ApiOperation(value = "Upload a File.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="multipart/form-data", required = true, dataType = "string", paramType = "header")    
    })            
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "SUCCESS", response = PropertyGroup.class),
    		@ApiResponse(code = 400, message = "INVALID REQUEST"), 
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"),
			@ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")    	
    })
	@Timed(name = "create")
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@Context final HttpServletRequest request) {
		long size = request.getContentLengthLong();
        if (size > 2097152) { //2mb
     	   throw new HengeException(Status.INTERNAL_SERVER_ERROR, "The field file exceeds its maximum permitted size of 2097152 bytes.");
        }
        try {
            final Part filePart = request.getPart("file");
            final Part fileVersionPart = request.getPart("data");
            final String fileName = filePart.getSubmittedFileName();
            
            try (InputStream fileInputStream = filePart.getInputStream();
                 InputStream dataInputStream = fileVersionPart.getInputStream()) {
                final FileVersion fileVersion = jsonUtils.fromJson(dataInputStream, FileVersion.class);
                final FileVersion fv = FileVersion.builder(fileVersion).withContent(IOUtils.toByteArray(fileInputStream)).withFileName(fileName).build();
                final FileVersion result = fileBD.create(fv);
                return Response.ok(result).build();
            }
            
        } catch (IOException | ServletException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "Error while processing the uploaded file", e);
        }
    }
	
	@ApiOperation(value = "Update the file by FileVersion name and version. ")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "SUCCESS", response = FileVersion.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "update")
	@PUT
	@Path("/update")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response update(@Context final HttpServletRequest request) {
		long size = request.getContentLengthLong();
        if (size > 2097152) { //2mb
     	   throw new HengeException(Status.INTERNAL_SERVER_ERROR, "The field file exceeds its maximum permitted size of 2097152 bytes.");
        }
        try {
            final Part filePart = request.getPart("file");
            final Part fileVersionPart = request.getPart("data");
            final String fileName = filePart.getSubmittedFileName();
            
            try (InputStream fileInputStream = filePart.getInputStream();
                 InputStream dataInputStream = fileVersionPart.getInputStream()) {
                final FileVersion fileVersion = jsonUtils.fromJson(dataInputStream, FileVersion.class);
                final FileVersion fv = FileVersion.builder(fileVersion).withContent(IOUtils.toByteArray(fileInputStream)).withFileName(fileName).build();
                final Optional<FileVersion> result = fileBD.update(fv.getName() ,fv);
                if(!result.isPresent()) {
                    throw new HengeResourceNotFoundException("No FileVersion was found by the given name ["+fv.getName()+"] to be updated. Consider creating a new one.");
                }
                return Response.ok(result.get()).build();
            }
            
        } catch (IOException | ServletException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "Error while processing the uploaded file", e);
        }
    }
	
	/**
	 * Deletes all the files of a {@link FileVersion} by name
	 * 
	 * @param name
	 *            the name of the {@link FileVersion} to delete
	 * @throws HengeResourceNotFoundException
	 */
	@ApiOperation(value = "Delete all versions of a FileVersion by name.", response = FileVersion.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue = "application/json", required = true, dataType = "string", paramType = "header") })
	@ApiResponses(value = { @ApiResponse(code = 204, message = "SUCCESS", response = FileVersion.class),
			@ApiResponse(code = 400, message = "INVALID REQUEST"),
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"), @ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"), @ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR") })
	@Timed(name = "deleteAllVersions")
	@DELETE
	@Path("/{fileVersionName}")
	public FileVersion delete(@PathParam(value = "fileVersionName") @ApiParam("fileVersionName") final String name)
			throws HengeResourceNotFoundException {

		LOGGER.info("FileRS :: Delete Start : FileVersionName [{}]", name);

		final Optional<FileVersion> entity = fileBD.delete(name);
		if (!entity.isPresent()) {
			throw new HengeResourceNotFoundException(
					"No FileVersion was found by the given name [" + name + "].");
		}

		LOGGER.info("FileRS :: Delete Start : FileVersionName [{}]", name);
		return entity.get();
	}

	/**
	 * Deletes a {@link FileVersion} by name and version
	 * 
	 * @param fileVersionName
	 *            the name of the {@link FileVersion} to delete
	 * @param fileVersionVersion
	 *            the version of the {@link FileVersion} to delete
	 * @throws HengeResourceNotFoundException
	 */
	@ApiOperation(value = "Delete a FileVersion by name and version.")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue = "application/json", required = true, dataType = "string", paramType = "header") })
	@ApiResponses(value = { @ApiResponse(code = 204, message = "SUCCESS"),
			@ApiResponse(code = 400, message = "INVALID REQUEST"),
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"), @ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"), @ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR") })
	@Timed(name = "deleteSpecificVersion")
	@DELETE
	@Path("/{fileVersionName}/versions/{fileVersionVersion}")
	public FileVersion delete(
			@PathParam(value = "fileVersionName") @ApiParam(value = "fileVersionName") final String fileVersionName,
			@PathParam(value = "fileVersionVersion") @ApiParam(value = "fileVersionVersion") final String fileVersionVersion)
			throws HengeResourceNotFoundException {

		LOGGER.info("FileVersionRS :: Delete Start : FileVersionName [{}], FileVersionVersion [{}]", fileVersionName,
				fileVersionVersion);

		final Optional<FileVersion> entity = fileBD.delete(fileVersionName, fileVersionVersion);
		if (!entity.isPresent()) {
			throw new HengeResourceNotFoundException("No FileVersion was found by the given name ["
					+ fileVersionName + "] and version [" + fileVersionVersion + "] to be deleted.");
		}

		LOGGER.info("FileVersionRS :: Delete End : FileVersionName [{}], FileVersionVersion [{}]", fileVersionName,
				fileVersionVersion);
		return entity.get();

	}

	/**
	 * Download the latest version of a {@link FileVersion} by name.
	 * 
	 * @param name
	 *            the name of the {@link FileVersion} to retrieve
	 * @return the File associated with the name
	 * @throws HengeResourceNotFoundException
	 */
	@ApiOperation(value = "Download the latest version of a FileVersion by name.", response = FileVersion.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue = "application/json", required = true, dataType = "string", paramType = "header") })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS", response = FileVersion.class),
			@ApiResponse(code = 400, message = "INVALID REQUEST"),
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"), @ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"), @ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR") })
	@Timed(name = "readLatestVersion")
	@GET
	@Path("/{fileVersionName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(@PathParam(value = "fileVersionName") @ApiParam("fileVersionName") final String name)
			throws HengeResourceNotFoundException {

		LOGGER.info("FileRS :: Download Start : FileVersionName [{}]", name);

		final Optional<FileVersion> entity = fileBD.read(name);
		if (!entity.isPresent()) {
			throw new HengeResourceNotFoundException(
					"No FileVersion was found by the given name [" + name + "].");
		}

		LOGGER.info("FileRS :: Download End : FileVersionName [{}] \nEntity :\n{}", name, entity);

		return downloadFile(entity.get());
	}

	/**
	 * Download the file by FileVersion name and version.
	 * 
	 * @param fileVersionName
	 *            the name of the {@link FileVersion} to retrieve
	 * @param fileVersionVersion
	 *            the version of the {@link FileVersion} to retrieve
	 * @return the {@link FileVersion} associated with the name and version
	 * @throws HengeResourceNotFoundException
	 */
	@ApiOperation(value = "Download the file by FileVersion name and version. ")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue = "application/json", required = true, dataType = "string", paramType = "header") })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS", response = FileVersion.class),
			@ApiResponse(code = 400, message = "INVALID REQUEST"),
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"), @ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"), @ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR") })
	@Timed(name = "readSpecificVersion")
	@GET
	@Path("/{fileVersionName}/versions/{fileVersionVersion}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(
			@PathParam(value = "fileVersionName") @ApiParam("fileVersionName") final String fileVersionName,
			@PathParam(value = "fileVersionVersion") @ApiParam("fileVersionName") final String fileVersionVersion)
			throws HengeResourceNotFoundException {

		LOGGER.info("FileVersionRS :: Download Start : FileVersionName [{}], FileVersionVersion[{}]", fileVersionName,
				fileVersionVersion);

		final Optional<FileVersion> entity = fileBD.read(fileVersionName, fileVersionVersion);
		if (!entity.isPresent()) {
			throw new HengeResourceNotFoundException("No FileVersion was found by the given name ["
					+ fileVersionName + "] and version [" + fileVersionVersion + "].");
		}
		return downloadFile(entity.get());
	}

	/**
	 * Retrieves the latest version number of a {@link FileVersion} by name.
	 * 
	 * @param name
	 *            the name of the {@link FileVersion} to retrieve
	 * @return the version associated with last {@link FileVersion} found for
	 *         name provided
	 * @throws HengeResourceNotFoundException
	 */
	@ApiOperation(value = "Read the latest version number of a FileVersion by name.", response = FileVersion.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue = "plain/text", required = true, dataType = "string", paramType = "header") })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "SUCCESS", response = FileVersion.class),
			@ApiResponse(code = 400, message = "INVALID REQUEST"),
			@ApiResponse(code = 401, message = "UNAUTHENTICATED"), @ApiResponse(code = 403, message = "UNAUTHORIZED"),
			@ApiResponse(code = 404, message = "NOT FOUND"), @ApiResponse(code = 409, message = "CONFLICT"),
			@ApiResponse(code = 500, message = "INTERNAL SERVER ERROR") })
	@Timed(name = "versionCeiling")
	@Produces("plain/text")
	@GET
	@Path("/{fileVersionName}/versions/ceiling")
	public String readLatestVersionNumber(
			@PathParam(value = "fileVersionName") @ApiParam("fileVersionName") final String name)
			throws HengeResourceNotFoundException {

		LOGGER.info("FileRS :: Read Latest Version Number Start : FileVersionName [{}]", name);

		final Optional<FileVersion> entity = fileBD.read(name);
		if (!entity.isPresent()) {
			throw new HengeResourceNotFoundException(
					"No FileVersion was found by the given name [" + name + "].");
		}

		LOGGER.info("FileRS :: Read End : FileVersionName [{}] \nEntity :\n{}", name, entity);
		return entity.get().getVersion();

	}
    
    /**
     * Return a set of version numbers associated with the {@link FileVersion} based on FileVersionName
     * @param fileVersionName
     * @return a set of {@link String} containing the existing versions of {@link FileVersion} with the given name
     */
    @ApiOperation(value = "Return a set of version numbers associated with a FileVersion by name.", response = FileVersion.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "SUCCESS", response = String.class, responseContainer = "Set"),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")     
    })
    @Timed(name = "versions")
    @GET
    @Path(value="/{fileVersionName}/versions")
    public Set<String> versions(@PathParam("fileVersionName") @ApiParam(value="fileVersionName") final String fileVersionName) {

        LOGGER.info("FileVersionRS :: Versions Start : FileVersionName [{}]", fileVersionName);
        
        final Optional<Set<String>> versions = fileBD.versions(fileVersionName);
        if(!versions.isPresent()) {
            throw new HengeResourceNotFoundException("No FileVersion was found by the given name ["+fileVersionName+"].");
        }
        
        LOGGER.info("FileVersionRS :: Versions End : FileVersionName [{}]", fileVersionName);     
        return versions.get();
        
    }   
    
    /**
     *Download the file by VersionSet name, VersionSet Version and file name
     * 
     */
    @ApiOperation(value="Download the file by VersionSet name, VersionSet version and file name")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })  
    @Timed(name = "readByVersionSet")
    @GET
    @Path("/{versionSetName}/{versionSetVersion}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFromVersionSet(@PathParam(value="versionSetName") final String versionSetName,
                                           @PathParam(value="versionSetVersion") final String versionSetVersion,
                                           @PathParam(value="fileName") final String fileName) {
                                            

        Optional<VersionSet> versionSet = versionSetBD.read(versionSetName, versionSetVersion);
        if( !versionSet.isPresent()){
            throw new HengeResourceNotFoundException("No VersionSet was found by the given name ["+versionSetName+"] and version ["+versionSetVersion+"].");
        }

        Set<FileVersionReference> fileVersionReferences = versionSet.get().getFileVersionReferences();
        
        java.util.Optional<FileVersion> fileVersion = fileVersionReferences.parallelStream()
        .filter(fileVersionReference -> fileVersionReference.getName().equals(fileName))
        .map(fileVersionReference -> fileBD.read(fileName, fileVersionReference.getVersion()).get())
        .findFirst();
        
        if(!fileVersion.isPresent()) {
            throw new HengeResourceNotFoundException("No File was found with the name ["+ fileName +"] in the given VersionSet ["+versionSetName+"] versionSet version ["+versionSetVersion+"].");
        }

        return downloadFile(fileVersion.get());
        
    }   
    
    @ApiOperation(value = "Get the current version of a specific FileVersion.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "SUCCESS", response = Response.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")
    })
    @Timed(name = "readCurrentVersion")
    @GET
    @Path("/current/{fileVersionName}")
    public Response getCurrentVersion(final @PathParam("fileVersionName") String fileVersionName) {
    	Optional<FileVersion> currentVersion = fileBD.getCurrentVersion(fileVersionName);
    	return Response.ok(currentVersion.get()).build();
    }
    
    @ApiOperation(value = "Set the current version of a specific FileVersion.")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "ACCEPT", value = "ACCEPT", defaultValue="application/json", required = true, dataType = "string", paramType = "header")
    })            
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "SUCCESS", response = Response.class),
            @ApiResponse(code = 400, message = "INVALID REQUEST"), 
            @ApiResponse(code = 401, message = "UNAUTHENTICATED"),
            @ApiResponse(code = 403, message = "UNAUTHORIZED"),
            @ApiResponse(code = 404, message = "NOT FOUND"),
            @ApiResponse(code = 409, message = "CONFLICT"),
            @ApiResponse(code = 500, message = "INTERNAL SERVER ERROR")
    })
    @Timed(name = "setCurrentVersion")
    @PUT
    @Path("/current/{fileVersionName}/{fileVersionVersion}")
    public Response setCurrentVersion(@PathParam("fileVersionName") final String fileVersionName, @PathParam("fileVersionVersion") final String fileVersionVersion) {
    	FileVersion currentVersion = fileBD.setCurrentVersion(fileVersionName, fileVersionVersion);
    	return Response.ok(currentVersion).build();

    }

	private Response downloadFile(final FileVersion entity) {
		StreamingOutput streaming = output -> {
			output.write(entity.getContent());
			output.flush();
		};
		return Response.ok(streaming)
				.header("Content-Disposition", "attachment; filename=" + entity.getFilename()).build();
	}

}
