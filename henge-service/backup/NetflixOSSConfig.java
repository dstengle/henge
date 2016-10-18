/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenzan.henge.config;

import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.netflix.appinfo.AmazonInfo;

@Component
@Configuration
@EnableEurekaClient
public class NetflixOSSConfig {

	@Bean
	@Profile("!default")
	public EurekaInstanceConfigBean eurekaInstanceConfig() {
		EurekaInstanceConfigBean b = new EurekaInstanceConfigBean();
		AmazonInfo info = AmazonInfo.Builder.newBuilder().autoBuild("eureka");
		b.setDataCenterInfo(info);
		return b;
	}
}