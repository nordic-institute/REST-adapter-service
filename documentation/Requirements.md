* It **must** be possible to access legacy WSDL-defined x-road services via rest/json. It is **not** necessary to generate RAML from WSDL
* It **must** be possible to access rest/json services via WSDL-defined x-road services. 
* WSDLs in the security server **must** match the actual service to facilitate service discovery (i.e. an "execute REST with payload" is not an OK x-road service)