package com.kenzan.henge.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("cassandra")
public class CassandraCleaner implements CleanerUtils {

	@Override
	public void execute() {
		//do nothing
	}

}
