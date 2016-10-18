package com.kenzan.henge.repository.impl.flatfile;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Considering the file implementation of the repositories, to optimize search
 * of resources, some data must stay in the name of the file, so it's not
 * necessary to open files during searches. In the case of this application, 
 * the name and version of the resource were chosen to be part of the file name.
 * This class provides a centralized implementation of the naming logic, that
 * will be used by the local and S3 implementations of the repository. 
 * It's important to keep it uniform in order to facilitate migration of data 
 * between them. 
 *
 * @author wmatsushita
 */
@Profile({"flatfile_local", "flatfile_s3"})
@Component
public class FileNamingService {

    // this token must not be present in the name of any entities
	public static final String FILE_NAME_SEPARATOR = "_-_";
	
    private static final String PATH_GLUE = "/";
    
    public <T> String getPath(Class<T> clazz, String... subFolders) {
        
        return (subFolders.length > 0) ? 
            new StringBuilder(clazz.getSimpleName()).append(PATH_GLUE).append(StringUtils.arrayToDelimitedString(subFolders, PATH_GLUE)).toString() 
            :
            clazz.getSimpleName();
        
    }

    public <T> String getCompleteFileName(String name, String version) {

        //defines the rule for composing the filename
        final StringBuilder fileName =
            new StringBuilder(name)
                .append(FILE_NAME_SEPARATOR)
                .append(version);

        return fileName.toString();
    }
    
    public <T> String getFileName(String name) {

        return new StringBuilder(name).append(FILE_NAME_SEPARATOR).toString();
        
    }
    
    public String extractEntityVersionFromFileName(final String fileName) {
        final String[] tokens = fileName.split(FILE_NAME_SEPARATOR); 
        
        //the last token is the version
        return tokens[tokens.length-1]; 
    }
    
    public String extractEntityNameFromFileName(final String fileName) {
    	final String[] tokens = fileName.split(FILE_NAME_SEPARATOR);
    	
    	/*
    	 *  Extract only the name, stripping away the version
    	 *  This way makes it insensitive to the token used to separate the name,
    	 *  meaning that the token can be present in the name of the entity, just not in the version.
    	 */
   		StringBuilder builder = new StringBuilder(tokens[0]);
    	for(int i=1; i<tokens.length-1; i++) {
    		builder.append(FILE_NAME_SEPARATOR);
    		builder.append(tokens[i]);
    	}
    	
    	return builder.toString();
    }
    
}
