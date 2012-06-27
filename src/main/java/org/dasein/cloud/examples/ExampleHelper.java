/**
 * ========= CONFIDENTIAL =========
 *
 * Copyright (C) 2012 enStratus Networks Inc - ALL RIGHTS RESERVED
 *
 * ====================================================================
 *  NOTICE: All information contained herein is, and remains the
 *  property of enStratus Networks Inc. The intellectual and technical
 *  concepts contained herein are proprietary to enStratus Networks Inc
 *  and may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law. Dissemination
 *  of this information or reproduction of this material is strictly
 *  forbidden unless prior written permission is obtained from
 *  enStratus Networks Inc.
 * ====================================================================
 */
package org.dasein.cloud.examples;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.MachineImageState;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.dc.DataCenter;

/**
 * Helper functions to support examples without cluttering them with irrelevant nonsense.
 * <p>Created by George Reese: 6/26/12 8:43 PM</p>
 * @author George Reese (george.reese@imaginary.com)
 * @since 2012.04
 * @version 2012.04 initial version
 */
public class ExampleHelper {
    private CloudProvider provider;
    
    public ExampleHelper(CloudProvider provider) { this.provider = provider; }

    public DataCenter findDataCenter() throws CloudException, InternalException {
        DataCenter any = null;
        for( DataCenter dc : provider.getDataCenterServices().listDataCenters(provider.getContext().getRegionId()) ) {
            if( dc.isActive() && dc.isAvailable() ) {
                return dc;
            }
            any = dc;
        }
        if( any != null ) {
            return any;
        }
        throw new CloudException("No data center was identified for example");
    }

    public MachineImage findImage() throws CloudException, InternalException {
        ComputeServices services = provider.getComputeServices();
        
        if( services == null ) {
            throw new CloudException("No compute services");
        }
        MachineImageSupport support = services.getImageSupport();
        
        if( support == null ) {
            throw new CloudException("No machine image support");
        }
        MachineImage any = null, best = null;
        
        for( MachineImage image : support.listMachineImages() ) {
            if( image.getCurrentState().equals(MachineImageState.DELETED) ) {
                continue;
            }
            if( any == null ) {
                any = image;
            }
            if( image.getCurrentState().equals(MachineImageState.ACTIVE) ) {
                if( image.getPlatform() != null && !image.getPlatform().isWindows() && !image.getPlatform().equals(Platform.UNKNOWN) ) {
                    if( best == null ) {
                        best = image;
                        any = best;
                    }
                    else {
                        if( Architecture.I64.equals(image.getArchitecture()) && !Architecture.I64.equals(best.getArchitecture()) ) {
                            best = image;
                            any = best;
                            break;
                        }
                    }
                }
            }
        }
        if( best == null ) {
            for( MachineImage image : support.searchMachineImages(null, Platform.UBUNTU, Architecture.I64) ) {
                if( image.getCurrentState().equals(MachineImageState.DELETED) ) {
                    continue;
                }
                if( any == null ) {
                    any = image;
                }
                if( image.getCurrentState().equals(MachineImageState.ACTIVE) ) {
                    best = image;
                    any = image;
                    break;
                }
            }
        }
        if( best != null ) {
            return best;
        }
        if( any != null ) {
            return any;
        }
        throw new CloudException("Unable to identify a valid machine image to use in the examples");
    }
    
    public VirtualMachineProduct findProduct(MachineImage forMachineImage) throws CloudException, InternalException {
        ComputeServices services = provider.getComputeServices();

        if( services == null ) {
            throw new CloudException("No compute services");
        }
        VirtualMachineSupport support = services.getVirtualMachineSupport();
        
        if( support == null ) {
            throw new CloudException("No virtual machine support");
        }
        VirtualMachineProduct best = null;

        for( VirtualMachineProduct product : support.listProducts(forMachineImage.getArchitecture()) ) {
            if( best == null ) {
                best = product;
            }
            else {
                if( product.getRamInMb() > 1000 ) {
                    if( product.getRamInMb() < best.getRamInMb() ) {
                        best = product;
                    }
                }
            }
        }
        if( best != null ) {
            return best;
        }
        throw new CloudException("Unable to identify a valid virtual machine product for " + forMachineImage.getProviderMachineImageId());
    }
}
