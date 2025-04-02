import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;

public class Main {
	   
	public static void main(String[] args) {
	        // Initialize CloudSim, Datacenter, and Broker
	        CloudSim.setup();

	        // Run scheduling algorithms
	        runFifo(cloudletList, vmList);
	        runShortestFirst(cloudletList, vmList);
	        runMinMin(cloudletList, vmList);
	        runMaxMin(cloudletList, vmList);
	        runSufferage();

	        // Print comparison table
	        printResults();
	    }

	    private static void runFifo(List<Cloudlet> cloudlets, List<Vm> vms) {
	    	 Queue<Cloudlet> queue = new LinkedList<>(cloudlets);

	         int vmIndex = 0; // Keep track of which VM to assign next

	         while (!queue.isEmpty()) {
	             Cloudlet cloudlet = queue.poll(); // Get the next task in the queue
	             Vm assignedVm = vms.get(vmIndex); // Assign it to a VM in round-robin order

	             cloudlet.setVmId(assignedVm.getId()); // Assign Cloudlet to VM

	             // Move to the next VM (round-robin style)
	             vmIndex = (vmIndex + 1) % vms.size(); 
	    	}
	    }
	    
	    private static void runShortestFirst(List<Cloudlet> cloudlets, List<Vm> vms) { 
	    	 // Sort cloudlets by length (ascending order)
	        Collections.sort(cloudlets, Comparator.comparingLong(Cloudlet::getCloudletLength));

	        int vmIndex = 0; // Track which VM is assigned next

	        for (Cloudlet cloudlet : cloudlets) {
	            Vm assignedVm = vms.get(vmIndex); // Get next VM
	            cloudlet.setVmId(assignedVm.getId()); // Assign Cloudlet to VM

	            // Move to the next VM (round-robin style)
	            vmIndex = (vmIndex + 1) % vms.size();
	    	}
	    }
	    
	    private static void runMinMin(List<Cloudlet> cloudlets, List<Vm> vms) { 
	    	 List<Cloudlet> remainingCloudlets = new ArrayList<>(cloudlets);
	         double[] vmLoad = new double[vms.size()]; // Track load on each VM

	         while (!remainingCloudlets.isEmpty()) {
	             Cloudlet minCloudlet = null;
	             int bestVmIndex = -1;
	             double minCompletionTime = Double.MAX_VALUE;

	             // Find the Cloudlet with the shortest execution time on any VM
	             for (Cloudlet cloudlet : remainingCloudlets) {
	                 for (int i = 0; i < vms.size(); i++) {
	                     double execTime = (cloudlet.getCloudletLength() / (double) vms.get(i).getMips()) + vmLoad[i];

	                     if (execTime < minCompletionTime) {
	                         minCompletionTime = execTime;
	                         minCloudlet = cloudlet;
	                         bestVmIndex = i;
	                     }
	                 }
	             }

	             // Assign the selected Cloudlet to the best VM
	             if (minCloudlet != null) {
	                 minCloudlet.setVmId(vms.get(bestVmIndex).getId());
	                 vmLoad[bestVmIndex] += minCloudlet.getCloudletLength() / (double) vms.get(bestVmIndex).getMips();
	                 remainingCloudlets.remove(minCloudlet);
	             }
	         }
	     }

	    
	    private static void runMaxMin(List<Cloudlet> cloudlets, List<Vm> vms) {
	        List<Cloudlet> remainingCloudlets = new ArrayList<>(cloudlets);
	        double[] vmLoad = new double[vms.size()]; // Track load on each VM

	        while (!remainingCloudlets.isEmpty()) {
	            Cloudlet maxCloudlet = null;
	            int bestVmIndex = -1;
	            double maxMinCompletionTime = -1;

	            // Find the Cloudlet with the longest execution time on any VM
	            for (Cloudlet cloudlet : remainingCloudlets) {
	                double minCompletionTime = Double.MAX_VALUE;
	                int selectedVmIndex = -1;

	                for (int i = 0; i < vms.size(); i++) {
	                    double execTime = (cloudlet.getCloudletLength() / (double) vms.get(i).getMips()) + vmLoad[i];

	                    if (execTime < minCompletionTime) {
	                        minCompletionTime = execTime;
	                        selectedVmIndex = i;
	                    }
	                }

	                // Select the task with the longest minimum completion time
	                if (minCompletionTime > maxMinCompletionTime) {
	                    maxMinCompletionTime = minCompletionTime;
	                    maxCloudlet = cloudlet;
	                    bestVmIndex = selectedVmIndex;
	                }
	            }

	            // Assign the selected Cloudlet to the best VM
	            if (maxCloudlet != null) {
	                maxCloudlet.setVmId(vms.get(bestVmIndex).getId());
	                vmLoad[bestVmIndex] += maxCloudlet.getCloudletLength() / (double) vms.get(bestVmIndex).getMips();
	                remainingCloudlets.remove(maxCloudlet);
	            }
	        }
	    }
	    
	    private static void runSufferage(List<Cloudlet> cloudlets, List<Vm> vms) {
	        List<Cloudlet> remainingCloudlets = new ArrayList<>(cloudlets);
	        double[] vmLoad = new double[vms.size()]; // Track load on each VM

	        while (!remainingCloudlets.isEmpty()) {
	            Cloudlet selectedCloudlet = null;
	            int bestVmIndex = -1;
	            double maxSufferage = -1;

	            // Find the cloudlet with the highest "sufferage" value
	            for (Cloudlet cloudlet : remainingCloudlets) {
	                double bestTime = Double.MAX_VALUE;
	                double secondBestTime = Double.MAX_VALUE;
	                int selectedVm = -1;

	                // Determine the two best completion times
	                for (int i = 0; i < vms.size(); i++) {
	                    double execTime = (cloudlet.getCloudletLength() / (double) vms.get(i).getMips()) + vmLoad[i];

	                    if (execTime < bestTime) {
	                        secondBestTime = bestTime;
	                        bestTime = execTime;
	                        selectedVm = i;
	                    } else if (execTime < secondBestTime) {
	                        secondBestTime = execTime;
	                    }
	                }

	                double sufferageValue = secondBestTime - bestTime;

	                // Select the cloudlet with the highest sufferage value
	                if (sufferageValue > maxSufferage) {
	                    maxSufferage = sufferageValue;
	                    selectedCloudlet = cloudlet;
	                    bestVmIndex = selectedVm;
	                }
	            }

	            // Assign the selected Cloudlet to the best VM
	            if (selectedCloudlet != null) {
	                selectedCloudlet.setVmId(vms.get(bestVmIndex).getId());
	                vmLoad[bestVmIndex] += selectedCloudlet.getCloudletLength() / (double) vms.get(bestVmIndex).getMips();
	                remainingCloudlets.remove(selectedCloudlet);
	            }
	        }
	    }
	    
	    private static void printResults() {
	    	/* Print the final comparison table */
	    	}
	}


