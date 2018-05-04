
package wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for calculate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="calculate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="g_x" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="g_y" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="g_z" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ap_x" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ap_y" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ap_z" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ap_pt" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="n" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="PL_1m" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="ld" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="conc" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="xml" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "calculate", propOrder = {
    "gx",
    "gy",
    "gz",
    "apX",
    "apY",
    "apZ",
    "apPt",
    "n",
    "pl1M",
    "ld",
    "conc",
    "xml"
})
public class Calculate {

    @XmlElement(name = "g_x")
    protected Integer gx;
    @XmlElement(name = "g_y")
    protected Integer gy;
    @XmlElement(name = "g_z")
    protected Integer gz;
    @XmlElement(name = "ap_x")
    protected Integer apX;
    @XmlElement(name = "ap_y")
    protected Integer apY;
    @XmlElement(name = "ap_z")
    protected Integer apZ;
    @XmlElement(name = "ap_pt")
    protected Integer apPt;
    protected Double n;
    @XmlElement(name = "PL_1m")
    protected Double pl1M;
    protected Double ld;
    protected Double conc;
    protected String xml;

    /**
     * Gets the value of the gx property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGX() {
        return gx;
    }

    /**
     * Sets the value of the gx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGX(Integer value) {
        this.gx = value;
    }

    /**
     * Gets the value of the gy property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGY() {
        return gy;
    }

    /**
     * Sets the value of the gy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGY(Integer value) {
        this.gy = value;
    }

    /**
     * Gets the value of the gz property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGZ() {
        return gz;
    }

    /**
     * Sets the value of the gz property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGZ(Integer value) {
        this.gz = value;
    }

    /**
     * Gets the value of the apX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getApX() {
        return apX;
    }

    /**
     * Sets the value of the apX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setApX(Integer value) {
        this.apX = value;
    }

    /**
     * Gets the value of the apY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getApY() {
        return apY;
    }

    /**
     * Sets the value of the apY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setApY(Integer value) {
        this.apY = value;
    }

    /**
     * Gets the value of the apZ property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getApZ() {
        return apZ;
    }

    /**
     * Sets the value of the apZ property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setApZ(Integer value) {
        this.apZ = value;
    }

    /**
     * Gets the value of the apPt property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getApPt() {
        return apPt;
    }

    /**
     * Sets the value of the apPt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setApPt(Integer value) {
        this.apPt = value;
    }

    /**
     * Gets the value of the n property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getN() {
        return n;
    }

    /**
     * Sets the value of the n property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setN(Double value) {
        this.n = value;
    }

    /**
     * Gets the value of the pl1M property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPL1M() {
        return pl1M;
    }

    /**
     * Sets the value of the pl1M property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPL1M(Double value) {
        this.pl1M = value;
    }

    /**
     * Gets the value of the ld property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLd() {
        return ld;
    }

    /**
     * Sets the value of the ld property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLd(Double value) {
        this.ld = value;
    }

    /**
     * Gets the value of the conc property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getConc() {
        return conc;
    }

    /**
     * Sets the value of the conc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setConc(Double value) {
        this.conc = value;
    }

    /**
     * Gets the value of the xml property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXml() {
        return xml;
    }

    /**
     * Sets the value of the xml property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXml(String value) {
        this.xml = value;
    }

}
