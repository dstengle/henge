# Henge

## Information
| Package       | Description   | Version| Build Status |
| ------------- |:-------------:| -----:|-----:|
| Henge| Store and retrieve dynamic configuration properties. | 0.9.0 | [![Build Status](https://travis-ci.org/kenzanlabs/henge.svg?branch=master)](https://travis-ci.org/kenzanlabs/henge) |

## Dependencies
* Java 1.8.x
* Maven

## Overview
From the dawn of time, developers have been storing configurations for their application in Java properties files. Whenever they pushed their application from one environment to the next (during deployment), they would request that the properties files be edited to reflect the unique values needed for each environment. After a while, even if the software could be produced as an artifact, the configs were hopelessly jumbled across the enterprise. Any exercise in resolving config values within the environment or across environments would lead to premature gray hair. 

Although many enterprises are now moving to key-value stores to retrieve properties and alleviate the file chaos, most of these key-value stores lack a complete hierarchical properties model that supports the application lifecycle. 

At Kenzan, we grew interested in this apparent property service gap through our own experience in developing a properties store using Netflix Archaius. In particular, we noted Archaius’ open issue [#132](https://github.com/Netflix/archaius/issues/132), which calls for creating a central properties service that separates the persistence of properties from Archaius. The question then became, how could we build something that fills this gap, is feature rich and marries well with modular application design? 

Enter Henge, a REST-based property server that aims to bring order to configuration properties in a number of ways:

* Make configs first class, versioned and immutable artifacts, just like software. 
* Group all possible environment values for a property in one place, preventing confusion and dismay.
* Define property groups for each of the libraries composing an application, and aggregate them into a single application config. 
* Put the configs on the network where they can be updated centrally for all applications.

Read the wiki [Henge Overview](https://github.com/kenzanlabs/henge/wiki/Henge-Overview) for more about how Henge functions in the software lifecycle. 

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

For how to run the application using S3 flatfile or Cassandra repositories, see [Repositories](https://github.com/kenzanlabs/henge/wiki/Repositories). 

## Usage
Adding and searching for Henge properties can be tested by running REST calls in the Postman collections that are available within the **/henge/documentation/demo** project folder. For a specific REST sequence that demonstrates Henge in Postman, see [Getting Started](https://github.com/kenzanlabs/henge/wiki/Getting-Started).

To see the complete API, visit the Swagger REST documentation at [http://localhost:8080/henge/swagger/index.html](http://localhost:8080/henge/swagger/index.html). 

The following Overview and Use Cases describe how Henge might be practically integrated in an application:

* [Henge Overview](https://github.com/kenzanlabs/henge/wiki/Henge-Overview) 
* [Use Case: Simple Application](https://github.com/kenzanlabs/henge/wiki/Use-Case:-Simple-Application)
* [Use Case: Complex Application](https://github.com/kenzanlabs/henge/wiki/Use-Case:-Complex-Application)

To run a simple application that implements Henge via Archaius, see the [Hello Properties demo](https://github.com/kenzanlabs/henge/wiki/Hello-Properties-Application). 

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
