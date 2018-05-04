
package wsdl;

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
@WebServiceClient(name = "MichaelService", targetNamespace = "http://web/", wsdlLocation = "http://wicaserv2-2.intec.ugent.be/MichaelService/Michael?WSDL")
public class MichaelService
    extends Service
{

    private final static URL MICHAELSERVICE_WSDL_LOCATION;
    private final static WebServiceException MICHAELSERVICE_EXCEPTION;
    private final static QName MICHAELSERVICE_QNAME = new QName("http://web/", "MichaelService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://wicaserv2-2.intec.ugent.be/MichaelService/Michael?WSDL");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        MICHAELSERVICE_WSDL_LOCATION = url;
        MICHAELSERVICE_EXCEPTION = e;
    }

    public MichaelService() {
        super(__getWsdlLocation(), MICHAELSERVICE_QNAME);
    }

    public MichaelService(WebServiceFeature... features) {
        super(__getWsdlLocation(), MICHAELSERVICE_QNAME, features);
    }

    public MichaelService(URL wsdlLocation) {
        super(wsdlLocation, MICHAELSERVICE_QNAME);
    }

    public MichaelService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, MICHAELSERVICE_QNAME, features);
    }

    public MichaelService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public MichaelService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns Michael
     */
    @WebEndpoint(name = "MichaelPort")
    public Michael getMichaelPort() {
        return super.getPort(new QName("http://web/", "MichaelPort"), Michael.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Michael
     */
    @WebEndpoint(name = "MichaelPort")
    public Michael getMichaelPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://web/", "MichaelPort"), Michael.class, features);
    }

    private static URL __getWsdlLocation() {
        if (MICHAELSERVICE_EXCEPTION!= null) {
            throw MICHAELSERVICE_EXCEPTION;
        }
        return MICHAELSERVICE_WSDL_LOCATION;
    }

}
