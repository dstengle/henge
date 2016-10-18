# Henge

## Information
| Package       | Description   | Version|
| ------------- |:-------------:| -----:|
| Henge| Store and retrieve dynamic configuration properties. | 0.9.0 |

## Dependencies:
   Java 1.8.x

## Overview
Henge is a flexible key/value store for dynamic configuration properties. The goal of the project is to produce a performant, reliable implementation of everything a production-ready property server application should provide. Because this functionality is composed of a modular architecture, you can customize the setup that works best for your organization.

The idea of creating Henge came from the Netflix Archiaus project, in particular the [open issue #132](https://github.com/Netflix/archaius/issues/132) which calls for creating a standard properties service. As described, the service would allow a PolledConfigurationSource on the client side to get all properties, which could then be changed dynamically on the server.

Henge supports pluggable persistence, life cycle management, validation, and querying capabilities of properties. A set of REST APIs are provided to interface with Henge and its properties.

## Install

The following gives a basic set of instructions for building Henge and running it using local flatfiles to store properties. The instructions apply to both Linux and macOS. 


1. Clone the Git repository: 
 
 ```
 git clone https://github.com/kenzanlabs/henge.git
 ```
2. Build the application: 
 
 ```
 mvn clean install
 ```
3. Run the application with local flatfiles: 
 
 ```
 mvn -pl henge-service spring-boot:run
 ```

For how to run the application using local flatfile or Cassandra repositories, see [Repositories](https://github.com/kenzanlabs/henge/wiki/Repositories). 

## Usage
Adding and searching for Henge properties can be tested by running REST calls in the Postman collections that are available within the **/henge/documentation/demo** project folder. For a specific REST sequence that demonstrates Henge in Postman, see [Getting Started](https://github.com/kenzanlabs/henge/wiki/Getting-Started).

To see the complete API, visit the Swagger REST documentation at:  

[http://localhost:8080/henge/swagger/index.html](http://localhost:8080/henge/swagger/index.html)

## Configuration
Henge uses Spring Profiles for runtime configuration. For information on configuring and running the application using various Spring Profiles, refer to the wiki page on [Profiles](https://github.com/kenzanlabs/henge/wiki/Profiles). 

## License
Copyright 2016 Kenzan, LLC <http://kenzan.com>
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at: 
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
