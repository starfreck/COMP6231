
package services;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "NorthAmericanServerImplService", targetNamespace = "http://server/", wsdlLocation = "http://localhost:8083/NorthAmerica?wsdl")
public class NorthAmericanServerImplService
    extends Service
{

    private final static URL NORTHAMERICANSERVERIMPLSERVICE_WSDL_LOCATION;
    private final static WebServiceException NORTHAMERICANSERVERIMPLSERVICE_EXCEPTION;
    private final static QName NORTHAMERICANSERVERIMPLSERVICE_QNAME = new QName("http://server/", "NorthAmericanServerImplService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8083/NorthAmerica?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        NORTHAMERICANSERVERIMPLSERVICE_WSDL_LOCATION = url;
        NORTHAMERICANSERVERIMPLSERVICE_EXCEPTION = e;
    }

    public NorthAmericanServerImplService() {
        super(__getWsdlLocation(), NORTHAMERICANSERVERIMPLSERVICE_QNAME);
    }

    public NorthAmericanServerImplService(WebServiceFeature... features) {
        super(__getWsdlLocation(), NORTHAMERICANSERVERIMPLSERVICE_QNAME, features);
    }

    public NorthAmericanServerImplService(URL wsdlLocation) {
        super(wsdlLocation, NORTHAMERICANSERVERIMPLSERVICE_QNAME);
    }

    public NorthAmericanServerImplService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, NORTHAMERICANSERVERIMPLSERVICE_QNAME, features);
    }

    public NorthAmericanServerImplService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public NorthAmericanServerImplService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns GameServer
     */
    @WebEndpoint(name = "NorthAmericanServerImplPort")
    public GameServer getNorthAmericanServerImplPort() {
        return super.getPort(new QName("http://server/", "NorthAmericanServerImplPort"), GameServer.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns GameServer
     */
    @WebEndpoint(name = "NorthAmericanServerImplPort")
    public GameServer getNorthAmericanServerImplPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://server/", "NorthAmericanServerImplPort"), GameServer.class, features);
    }

    private static URL __getWsdlLocation() {
        if (NORTHAMERICANSERVERIMPLSERVICE_EXCEPTION!= null) {
            throw NORTHAMERICANSERVERIMPLSERVICE_EXCEPTION;
        }
        return NORTHAMERICANSERVERIMPLSERVICE_WSDL_LOCATION;
    }

}
