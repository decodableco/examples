/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.demos.webhooks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class App {
    
    public static void main(String[] args) {
    	List<Committer> committers = Arrays.asList(
    			new Committer("Catalina Lehner", "catalina.lehner@example.com", 0.3),
    			new Committer("Omer Kreiger", "omer.kreiger@example.com", 0.10),
    			new Committer("Thuy Schamberger", "thuy.schamberger@example.com", 0.20),
    			new Committer("Gerry Marquardt", "gerry.marquardt@example.com", 0.05),
    			new Committer("Edward Kuhlman", "edward.kuhlman@example.com", 0.03),
    			new Committer("Kent Weissnat", "kent.weissnat@example.com", 0.07),
    			new Committer("Denver Rosenbaum", "denver.rosenbaum@example.com", 0.09),
    			new Committer("Ruben Runolfsson", "ruben.runolfsson@example.com", 0.02),
    			new Committer("Jone Collier", "jone.collier@example.com", 0.08),
    			new Committer("Alexander Davis", "alexander.davis@example.com", 0.06)
        );
    	
    	double sum = 0;
    	Map<Committer, Double> accumulatedWeight = new LinkedHashMap<>(); 
    	for (Committer committer : committers) {
    		sum += committer.weight;
    		accumulatedWeight.put(committer, sum);
		}
    	
    	Random random = new Random();
    	double value = random.nextDouble();
		
		for (Entry<Committer, Double> committer : accumulatedWeight.entrySet()) {
			if(value < committer.getValue()) {
				System.out.println(committer.getKey().name() + " <" + committer.getKey().email + ">");
				break;
			}
		}  	
	}
    
    private record Committer(String name, String email, double weight) {
    }
}
