package hirs.attestationca.persist.util;

import com.github.marandus.pciid.model.Device;
import com.github.marandus.pciid.model.Vendor;
import com.github.marandus.pciid.service.PciIdsDatabase;
import com.google.common.base.Strings;
import hirs.attestationca.persist.entity.userdefined.certificate.attributes.ComponentIdentifier;
import hirs.attestationca.persist.entity.userdefined.certificate.attributes.V2.ComponentIdentifierV2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERUTF8String;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provide Java access to PCI IDs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PciIds {
    /**
     * This pci ids file can be in different places on different distributions.
     */
    public static final List<String> PCI_IDS_PATH =
            Collections.unmodifiableList(new ArrayList<>() {
                private static final long serialVersionUID = 1L;
                {
                    add("/usr/share/hwdata/pci.ids");
                    add("/usr/share/misc/pci.ids");
                    add("/tmp/pci.ids");
                }
            });

    /**
     * The PCI IDs Database object.
     *
     * This only needs to be loaded one time.
     *
     * The pci ids library protects the data inside the object by making it immutable.
     */
    public static final PciIdsDatabase DB = new PciIdsDatabase();

    static {
        if (!DB.isReady()) {
            String dbFile = null;
            for (final String path : PCI_IDS_PATH) {
                if ((new File(path)).exists()) {
                    dbFile = path;
                    break;
                }
            }
            if (dbFile != null) {
                InputStream is = null;
                try {
                    is = new FileInputStream(new File(dbFile));
                    DB.loadStream(is);
                } catch (IOException e) {
                    // DB will not be ready, hardware IDs will not be translated
                    dbFile = null;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            dbFile = null;
                        }
                    }
                }
            }
        }
    }

    /**
     * The Component Class TCG Registry OID.
     */
    public static final String COMPCLASS_TCG_OID = "2.23.133.18.3.1";
    /**
     * The Component Class Value mask for NICs.
     */
    public static final String COMPCLASS_TCG_CAT_NIC = "00090000";
    /**
     * The Component Class Value mask for GFX cards.
     */
    public static final String COMPCLASS_TCG_CAT_GFX = "00050000";

    /**
     * Iterate through all components and translate PCI hardware IDs as necessary.  It will only
     * translate ComponentIdentifierV2+ objects as it relies on Component Class information.
     * @param components List of ComponentIdentifiers.
     * @return the translated list of ComponentIdentifiers.
     */
    public static List<ComponentIdentifier> translate(
            final List<ComponentIdentifier> components) {
        List<ComponentIdentifier> newList = new ArrayList<>();
        if (components != null && !components.isEmpty()) {
            for (final ComponentIdentifier component : components) {
                // V2 components should not be found alongside V1 components
                // they pass through just in case
                if (component.isVersion2()) {
                    newList.add(translate((ComponentIdentifierV2) component));
                } else {
                    newList.add(component);
                }
            }
        }
        return newList;
    }

    /**
     * Translate Vendor and Device IDs, if found, in ComponentIdentifierV2 objects.
     * It will only translate ID values, any other value will pass through.
     * @param component ComponentIdentifierV2 object.
     * @return the translated ComponentIdentifierV2 object.
     */
    public static ComponentIdentifierV2 translate(final ComponentIdentifierV2 component) {
        ComponentIdentifierV2 newComponent = null;
        if (component != null) {
            newComponent = component;
            // This can be updated as we get more accurate component class registries and values
            // Component Class Registry not accessible: TCG assumed
            final String compClassValue = component.getComponentClass().getCategory();
            if (compClassValue.equals(COMPCLASS_TCG_CAT_NIC)
                    || compClassValue.equals(COMPCLASS_TCG_CAT_GFX)) {
                DERUTF8String manufacturer = (DERUTF8String) translateVendor(component.getComponentManufacturer());
                DERUTF8String model = (DERUTF8String) translateDevice(component.getComponentManufacturer(),
                        component.getComponentModel());

                newComponent = new ComponentIdentifierV2(component.getComponentClass(),
                        manufacturer,
                        model,
                        component.getComponentSerial(),
                        component.getComponentRevision(),
                        component.getComponentManufacturerId(),
                        component.getFieldReplaceable(),
                        component.getComponentAddress(),
                        component.getCertificateIdentifier(),
                        component.getComponentPlatformUri(),
                        component.getAttributeStatus());
            }

        }
        return newComponent;
    }

    /**
     * Look up the vendor name from the PCI IDs list, if the input string contains an ID.
     * If any part of this fails, return the original manufacturer value.
     * @param refManufacturer DERUTF8String, likely from a ComponentIdentifier
     * @return DERUTF8String with the discovered vendor name, or the original manufacturer value.
     */
    public static ASN1UTF8String translateVendor(final ASN1UTF8String refManufacturer) {
        ASN1UTF8String manufacturer = refManufacturer;
        if (manufacturer != null && manufacturer.getString().trim().matches("^[0-9A-Fa-f]{4}$")) {
            Vendor ven = DB.findVendor(manufacturer.getString().toLowerCase());
            if (ven != null && !Strings.isNullOrEmpty(ven.getName())) {
                manufacturer = ASN1UTF8String.getInstance(ven.getName());
            }
        }
        return manufacturer;
    }

    /**
     * Look up the device name from the PCI IDs list, if the input strings contain IDs.
     * The Device lookup requires the Vendor ID AND the Device ID to be valid values.
     * If any part of this fails, return the original model value.
     * @param refManufacturer ASN1UTF8String, likely from a ComponentIdentifier
     * @param refModel ASN1UTF8String, likely from a ComponentIdentifier
     * @return ASN1UTF8String with the discovered device name, or the original model value.
     */
    public static ASN1UTF8String translateDevice(final ASN1UTF8String refManufacturer,
                                                final ASN1UTF8String refModel) {
        ASN1UTF8String manufacturer = refManufacturer;
        ASN1UTF8String model = refModel;
        if (manufacturer != null
                && model != null
                && manufacturer.getString().trim().matches("^[0-9A-Fa-f]{4}$")
                && model.getString().trim().matches("^[0-9A-Fa-f]{4}$")) {
            Device dev = DB.findDevice(manufacturer.getString().toLowerCase(),
                    model.getString().toLowerCase());
            if (dev != null && !Strings.isNullOrEmpty(dev.getName())) {
                model = ASN1UTF8String.getInstance(dev.getName());
            }
        }
        return model;
    }
}
