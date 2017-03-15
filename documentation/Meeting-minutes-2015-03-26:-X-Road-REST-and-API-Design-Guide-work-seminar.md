# **X-Road REST and API Design Guide work seminar - meeting minutes**

Date: 2015-03-36
Venue: VRK, Helsinki, Finland

**Participants**
* Petteri Kivimäki, VRK
* Andres Kütt, RIA, Estonia
* Markus Törnqvist, Codento
* Jari Hirvonen, Codento
* Jarkko Moilanen, Ministry of Education and Culture
* Taija Björklund, Sampo Software

_Participating via teleconference:_
* Karri Niemelä, BeAn Solutions 
* Pekka Muhonen, CSC
* Mikko Pitkänen, PRH

## Status by Petteri
* 1st version of REST support in January
* February: new features > further development of the component
* Currently supported HTTP verbs: get, post, put, delete

## Estonia status by Andres
Have had sessions, where they have opened the REST-support related code, no code contributions yet.

## Slideshow about REST Gateway and live demo by Petteri
* Comment from Andres: The implemented REST Gateway has no security measures, you have to make sure that only organisations that are allowed to use it, can have access to it
* REST Gateway: No support for multiple URL’s at the moment
* Finland has adopted the following rule: Security server must be security server and nothing else
* Future development ideas: 
 * UI for managing configuration
 * Packaging
 * Security measures (~ browser-based access, map the id from the http request with the actual request?? Whoever makes the request has the right credentials? SSO or filtering required? Don’t want to build this to the gateway probably. Both session-based and algorithm-based needed?)
 * RAML > WSDL conversion (Is there need for this? The key question is to have valid WSDL without having too tight specifications???)

## Pattern documentation  / Andres
* Andres presented 2 documents written by him:
 * Patterns
 * 2 templates for the most “interesting” patterns (Asynchronous pattern and Reconciliator), interesting here referring to the ones that are used most often
* How to proceed with the documents? On-line document writing tool > Can start working on them together. Andres will share the link
* X-Road workshop in May > **AP**: Andres to make a presentation about patterns

## REST API Standard
* Based on the White House Web API Standards > Jarkko has made a fork of this in GitHub > mirror this, make possible changes and suggest them back to the original
* Is the original copyrighted material or public domain? **AP**: Jarkko to find out
* Named: **X-Road REST API Standards [draft]**
* Probable mandatory changes to the original: UTF-8, and authentication + authorization
 * **AP** Andres: Add UTF-8
 * Discussion about authentication and authorization: should you recommend that these are carried out - but are they in the scope of this documentation? Or should there be a separate security guideline? 
* Working language English for dratf (0.x) versions, but when the standard reaches approved (1.0) version, it must be translated into Finnish and Swedish. After that keep observing the original White house standard and translate when necessary.
* Separate design guideline about API design to be created, named “API Design guidelines”. Goal is to make the work of the API developers easier.
 * How to start with the API Design Guidelines? 
 * Andres suggested that this might be linked to Service catalogue? Petteri commented that VRK is writing a report on different approaches on how to implement Service catalogue in Finland.
 * Discussion about whether this is needed or not: 
   * If you don’t document API design guidelines, there is low barrier for anyone to get in, but the situation with the implementations might get quite wild. Is this a problem or not?
   * You could also use this to change thinking: API first.
  * To be considered / decided later.
* A new README was written: introduction, list of documents, how to contribute, licensing
* Licensing for the X-Road REST API Standards: According to the official recommendation, the options are CC BY 4.0 or CC0. 
* Who is the maintainer of the documentation and what is the workflow?
 * Workflow: Use pull requests to commit contributions. 
 * Maintainer: Karri Niemelä.
