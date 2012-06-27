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
package org.dasein.cloud.examples.compute;

import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineProduct;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.dc.DataCenter;
import org.dasein.cloud.examples.ExampleHelper;

import java.util.Arrays;

/**
 * [Basic examples for using servers with Dasein Cloud.
 * <p>Created by George Reese: 6/26/12 8:20 PM</p>
 * @author George Reese (george.reese@enstratus.com)
 * @since 2012.04
 * @version 2012-04 initial version
 */
public class ServerExample {
    /**
     * Runs the sample code against any cloud. Note that how you interpret account number, access key, and private key
     * depend on the authentication used by the cloud in question. Account number generally identifies a specific
     * account in the cloud, and the access keys could be normal access keys or user name and passwords. For some
     * clouds, account number and access key public may be the same thing.
     * @param args [DASEIN CLOUD PROVIDER IMPLEMENTATION] [ACCOUNT NUMBER] [ACCESS KEY PUBLIC] [ACCESS KEY PRIVATE] [ENDPOINT] [REGION ID]
     * @throws Exception any error that occurs while running the example
     */
    static public void main(String ... args) throws Exception {
        CloudProvider provider = (CloudProvider)Class.forName(args[0]).newInstance();
        ProviderContext ctx = new ProviderContext();

        ctx.setAccountNumber(args[1]);
        ctx.setAccessPublic(args[2].getBytes("utf-8"));
        ctx.setAccessPrivate(args[3].getBytes("utf-8"));
        ctx.setEndpoint(args[4]);
        ctx.setRegionId(args[5]);
        provider.connect(ctx);
        ComputeServices services = provider.getComputeServices();
        
        if( services == null ) {
            System.out.println("The cloud " + provider.getCloudName() + " does not support compute services.");
            return;
        }
        
        VirtualMachineSupport support = services.getVirtualMachineSupport();
        
        if( support == null ) {
            System.out.println("The cloud " + provider.getCloudName() + " does not support virtual machines.");
            return;
        }
        try {
            ServerExample runner = new ServerExample(provider, support);
            
            runner.run();
        }
        finally {
            provider.close();
        }
    }
    
    private CloudProvider         provider;
    private VirtualMachineSupport vmSupport;
    
    private ServerExample(CloudProvider provider, VirtualMachineSupport support) { 
        this.provider = provider;
        this.vmSupport = support;
    }

    private void exampleGetServer(VirtualMachine vm) throws Exception {
        System.out.println("EXAMPLE: Get Server");

        VirtualMachine get = vmSupport.getVirtualMachine(vm.getProviderVirtualMachineId());
        
        System.out.println("Got: " + get);
        System.out.println("");
    }

    private VirtualMachine exampleLaunchServer() throws Exception {
        System.out.println("EXAMPLE: Launch Server");
        ExampleHelper helper = new ExampleHelper(provider);

        MachineImage image = helper.findImage();
        VirtualMachineProduct product = helper.findProduct(image);
        DataCenter dc = helper.findDataCenter();
        
        System.out.println("Launching server with machine image=" + image.getProviderMachineImageId() + ", product=" + product.getProductId() + ", and dc=" + dc.getProviderDataCenterId());
        VirtualMachine vm = vmSupport.launch(image.getProviderMachineImageId(), product, dc.getProviderDataCenterId(), "Example Server", "Example server from Dasein Cloud examples", null, null, false, false, new String[0]);
        
        System.out.println("ID:    " + vm.getProviderVirtualMachineId());
        System.out.println("Name:  " + vm.getName());
        System.out.println("OS:    " + vm.getPlatform());
        System.out.println("IPs:   " + Arrays.toString(vm.getPrivateIpAddresses()));
        System.out.println("State: " + vm.getCurrentState().name());
        System.out.println("");
        return vm;
    }
            
    private void exampleListServers() throws Exception {
        System.out.println("EXAMPLE: List Servers");
        System.out.println("Listing servers in " + provider.getCloudName() + ":");
        for( VirtualMachine vm : vmSupport.listVirtualMachines() ) {
            System.out.println("\t* " + vm.toString());
        }
        System.out.println("");
    }

    private void exampleTerminateServer(VirtualMachine vm) throws Exception {
        System.out.println("EXAMPLE: Terminate Server");
        System.out.println("Terminating " + vm);

        vmSupport.terminate(vm.getProviderVirtualMachineId());
        System.out.println("");
    }
    
    private void run() throws Exception {
        VirtualMachine vm;
        
        exampleListServers();
        vm = exampleLaunchServer();
        exampleGetServer(vm);
        exampleTerminateServer(vm);
    }
}
