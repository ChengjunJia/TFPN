package chengjun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.logging.*;

import bean.Network;
import bean.basis.Node;
import bean.basis.Rule;

import factory.HeaderFactory;
import factory.TransferFuncFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

// ATPG algorithm
public class atpg {
	public static void main(String args[]) throws SecurityException, IOException {
		LogManager.getLogManager().reset(); // Close the logging
		Logger logger = Logger.getLogger("ATPG");
		logger.setLevel(Level.INFO);

		int inputID = Integer.parseInt(args[0]);
		if (inputID > 3 || inputID < 0) {
			System.out.printf("We only have 3 test cases!\n");
			return;
		}
		ArrayList<String> networksName = new ArrayList<String>(
				Arrays.asList("simple_stanford.network", "2_4_4.network", "4_8_16.network"));
		Path path = Paths.get("examples", networksName.get(inputID));
		ArrayList<String> loggerNames = new ArrayList<String>(
				Arrays.asList("run_stanford.log", "run_fattree_2.log", "run_fattree_4.log"));
		int bitSize = 34;
		if (inputID == 0) {
			bitSize = 32; // 32 bit for StanfordSimple, 34 bit for fattree
		}

		FileHandler fh = new FileHandler(loggerNames.get(inputID));
		logger.addHandler(fh);
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
		fh.setFormatter(new SimpleFormatter());

		Network network = new Network();
		network.importFromFile(path.toString());

		Node Pkt = new Node();
		Pkt.setHdr(HeaderFactory.generateInputHeader(bitSize, 'x'));
		// Pkt.setHdr(HeaderFactory.generateHeader(APVTransFunc.predicates.size()));

		// Get all ports here. TTF is to get the Transfer Rules, i.e. the fwd rules
		ArrayList<Rule> topoRules = network.getTTF().rules;
		ArrayList<Rule> netRules = network.getNTF().rules;
		LinkedHashSet<Integer> netPortSet = new LinkedHashSet<Integer>();
		for (Rule r : netRules) {
			for (Integer p : r.getInPorts()) {
				netPortSet.add(p);
			}
			// Only calculate the input ports of network rules!
			// for (Integer p : r.getOutPorts()) {
			// netPortSet.add(p);
			// }
		}
		ArrayList<Integer> netPortList = new ArrayList<>(netPortSet);
		int netPortNum = netPortList.size();
		int ruleNum = netRules.size();
		int pathNum = 0;
		ArrayList<Node> allPath = new ArrayList<Node>();

		Level currentLevel = logger.getLevel();
		if (currentLevel == null && logger.getParent().getLevel() != null) {
			currentLevel = logger.getParent().getLevel();
		}
		logger.info(
				"CurrentLevel as " + currentLevel.intValue() + " and Fine level " + Level.FINE.intValue());
		boolean skip_trace_result = false;
		if (currentLevel.intValue() > Level.FINE.intValue()) {
			skip_trace_result = true;
		}
		// ArrayList<Node>
		long start = System.nanoTime();
		for (int i = 0; i < netPortList.size(); i++) {
			Pkt.setPort(netPortList.get(i));
			for (int j = 0; j < netPortList.size(); j++) {
				ArrayList<Integer> Ports = new ArrayList<Integer>();
				if (i == j) {
					continue;
				}
				Ports.add(netPortList.get(j));
				// Ports.addAll(netPortList);
				// Ports.remove(i); // Add all ports but remove the local to avoid loop!
				ArrayList<Node> result = TransferFuncFactory.findReachabilityByPropagation(network.getNTF(),
						network.getTTF(), Pkt, Ports);
				allPath.addAll(result);
				logger.info("Node " + netPortList.get(i) + "->" + netPortList.get(j) + ", path num: " + result.size());
				if (result.size() == 0) {
					continue;
				}
				// IGNORE THE OUTPUT
				if (skip_trace_result) {
					continue;
				}
				for (Node n : result) {
					if (n.getVisits().size() < 1000) {
						ArrayList<Rule> appliedRules = n.getRuleHistory();
						if (appliedRules.size() == 1) {
							continue;
						}
						String a = new String("");
						for (Rule r : appliedRules) {
							a = a + " " + r.getId();
						}
						a = a + ".";
						logger.fine("Applied rule# is " + appliedRules.size() + " " + a);
					}
				}
				pathNum += result.size();
			}
		}
		long end = System.nanoTime();

		logger.info("Network ports number is " + netPortNum + "; link# is " + topoRules.size() + "; rule# is "
				+ ruleNum + "; path# is " + allPath.size());
		logger.info("We run the ATPG program with " + (end - start) / 1000 + " us");

		for (int mode = 0; mode < 2; mode++) {
			ArrayList<Node> lastResult = allPath;
			ArrayList<Node> reducedResult = new ArrayList<Node>();

			// Reduce all paths in the last result
			LinkedHashSet<Rule> visitedRules = new LinkedHashSet<Rule>();
			LinkedHashSet<Integer> visitedPorts = new LinkedHashSet<Integer>();

			Collections.shuffle(lastResult);
			start = System.nanoTime();
			int reduce_time = 0;
			for (int i = 0; i < 1000; i++) {
				reduce_time++;
				for (Node n : lastResult) {
					ArrayList<Rule> appliedRules = n.getRuleHistory();
					ArrayList<Integer> appliedPort = n.getVisits();
					if (mode == 0) {
						for (Rule r : appliedRules) { // Cover all rules
							if (!visitedRules.contains(r)) {
								visitedRules.addAll(appliedRules);
								reducedResult.add(n);
								break;
							}
						}

					} else if (mode == 1) {
						for (Integer portID : appliedPort) { // Cover all ports
							if (!visitedPorts.contains(portID)) {
								visitedPorts.addAll(appliedPort);
								reducedResult.add(n);
								break;
							}
						}
					}
				}
				logger.info("Reduce the path number from " + lastResult.size() + " to " + reducedResult.size());
				if (lastResult.size() == reducedResult.size()) {
					break; // We can not further reduce the size, break!
				}
				// Next iteration
				lastResult = reducedResult;
				Collections.shuffle(lastResult);
				reducedResult = new ArrayList<Node>();
				visitedRules.clear();
				visitedPorts.clear();
			}
			end = System.nanoTime();

			ArrayList<Node> finalPaths = reducedResult;

			int total_path = 0;
			for (Node path_tmp : finalPaths) {
				total_path += path_tmp.getRuleHistory().size() + 1;
			}

			logger.info("We run " + reduce_time + " times to reduce to " + finalPaths.size() + " with time "
					+ (end - start) / 1000 + " us" + " with #ports: " + visitedPorts.size() + ", #rules: "
					+ visitedRules.size() + ", #totalPath: " + total_path);
		}
		// Get the rule id list
		// for (Rule r : topoRules) {
		// String rule_id = r.getId();
		// System.out.println("Topo rule ID is " + rule_id);
		// }
		// for (Rule r : netRules) {
		// String rule_id = r.getId();
		// System.out.println("Network rule ID is " + rule_id);
		// }

	}
}
