package chengjun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.logging.*;

import bean.Network;
import bean.basis.Node;
import bean.basis.Rule;

import factory.HeaderFactory;
import factory.TransferFuncFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import hassel.bean.HSATransFunc;

// Increment algorithm
public class increment {
    public static void main(String args[]) throws SecurityException, IOException {
        LogManager.getLogManager().reset(); // Close the logging
        Logger logger = Logger.getLogger("Increment");
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
                Arrays.asList("run_stanford_increment.log", "run_fattree_2_increment.log",
                        "run_fattree_4_increment.log"));
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

        // TODO: Get the graph here (topoRules, a-->b; netRules, [a1, a2]-->b)
        // TODO: how to get the results here?
        ArrayList<Rule> allRules = new ArrayList<Rule>();
        allRules.addAll(topoRules);
        allRules.addAll(netRules);
        HashMap<Integer, Integer> port2matID = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> matID2port = new HashMap<Integer, Integer>();
        LinkedHashSet<Integer> allPorts = new LinkedHashSet<Integer>();
        LinkedHashSet<Integer> topoPorts = new LinkedHashSet<Integer>();
        for (Rule r : allRules) {
            allPorts.addAll(r.getInPorts());
            allPorts.addAll(r.getOutPorts());
        }
        for (Rule r : topoRules) {
            topoPorts.addAll(r.getInPorts());
            topoPorts.addAll(r.getOutPorts());
        }
        Integer matIDIndex = 0;
        for (Integer portID : allPorts) {
            port2matID.put(portID, matIDIndex);
            matID2port.put(matIDIndex, portID);
            matIDIndex++;
        }
        int portNum = allPorts.size();
        logger.info("The total port number is " + portNum);
        boolean[][] connectMat = new boolean[portNum][];
        for (int i = 0; i < portNum; i++) {
            connectMat[i] = new boolean[portNum];
            for (int j = 0; j < portNum; j++) {
                connectMat[i][j] = false;
            }
        }
        for (Rule r : allRules) {
            for (Integer p : r.getInPorts()) {
                for (Integer q : r.getOutPorts()) {
                    // p --> q
                    if (p.equals(q)) { // CRITICAL: we must use equal here
                        continue;
                    }
                    int pInMat = port2matID.get(p);
                    int qInMat = port2matID.get(q);
                    connectMat[pInMat][qInMat] = true;
                    // logger.info("We have " + pInMat + "->" + qInMat + " <==> " + p + " ->" + q);
                }
            }
        }
        // Find the switches
        LinkedHashSet<Integer> switchSet = topoPorts;
        LinkedHashSet<Integer> endHostSet = new LinkedHashSet<Integer>();
        for (Integer portID : allPorts) {
            if (!topoPorts.contains(portID)) {
                endHostSet.add(portID);
            }
        }
        logger.info("The switchSet is " + switchSet);
        logger.info("The endhostSet is " + endHostSet);
        // Find the endhosts

        // ArrayList<Integer> netPortList = new ArrayList<>(endHostSet);

        ArrayList<Integer> netPortList = new ArrayList<>(netPortSet);
        int netPortNum = netPortList.size();
        int ruleNum = netRules.size();
        int pathNum = 0;
        ArrayList<Node> allPath = new ArrayList<Node>();

        Level currentLevel = logger.getLevel();
        if (currentLevel == null && logger.getParent().getLevel() != null) {
            currentLevel = logger.getParent().getLevel();
        }

        // Start to get the path
        LinkedHashSet<Rule> visitedRules = new LinkedHashSet<Rule>();
        LinkedHashSet<Integer> visitedPorts = new LinkedHashSet<Integer>();
        LinkedHashSet<Rule> unvisitedRules = new LinkedHashSet<Rule>();
        LinkedHashSet<Integer> unvisitedPorts = new LinkedHashSet<Integer>();
        LinkedHashSet<Integer> unvisitedEndPorts = new LinkedHashSet<Integer>();
        unvisitedRules.addAll(netRules);
        unvisitedPorts.addAll(netPortList);
        unvisitedEndPorts.addAll(endHostSet);

        LinkedHashSet<Integer> visitedStartPorts = new LinkedHashSet<Integer>();

        long start = System.nanoTime();
        while (unvisitedEndPorts.size() > 0) {
            // First Step, choose the path with longest pair and then others
            Integer start_port = 0;
            for (Integer i : unvisitedEndPorts) {
                start_port = i;
                break;
            }

            Pkt = new Node();
            Pkt.setHdr(HeaderFactory.generateInputHeader(bitSize, 'x'));
            Pkt.setPort(start_port);
            ArrayList<Integer> Ports = new ArrayList<Integer>();
            Ports.addAll(endHostSet);
            Ports.remove(start_port); // Add all ports but remove the local to avoid loop!
            ArrayList<Node> result = TransferFuncFactory.findReachabilityByPropagation(network.getNTF(),
                    network.getTTF(), Pkt, Ports);
            allPath.addAll(result);
            logger.info("Node " + start_port + "->*, path num: " + result.size());

            for (Node n : result) {
                ArrayList<Rule> appliedRules = n.getRuleHistory();
                ArrayList<Integer> appliedPorts = n.getVisits();
                visitedRules.addAll(appliedRules);
                visitedPorts.addAll(appliedPorts);
                unvisitedRules.removeAll(appliedRules);
                unvisitedPorts.removeAll(appliedPorts);
                // logger.info("Before we have ports " + unvisitedEndPorts.size());
                unvisitedEndPorts.removeAll(appliedPorts);
                // logger.info("After we have ports " + unvisitedEndPorts.size());
            }
            visitedPorts.add(start_port);
            unvisitedPorts.remove(start_port);
            unvisitedEndPorts.remove(start_port);
            visitedStartPorts.add(start_port);

            pathNum += result.size();

            pathNum += result.size();
        }
        assert (allPath.size() == pathNum);
        logger.info("Network ports number is " + netPortNum + "; link# is " + topoRules.size() + "; rule# is "
                + ruleNum + "; path# is " + allPath.size() + " to cover all endports");
        logger.info("We have cover/uncover " + visitedRules.size() + "/" + unvisitedRules.size() + " rules and "
                + visitedPorts.size() + "/" + unvisitedPorts.size() + " ports");
        int useless_try = 0;
        // Traverse all unvisited ports
        for (Integer start_port : unvisitedPorts) {
            Pkt = new Node();
            Pkt.setHdr(HeaderFactory.generateInputHeader(bitSize, 'x'));
            Pkt.setPort(start_port);
            ArrayList<Integer> Ports = new ArrayList<Integer>();
            Ports.addAll(endHostSet);
            Ports.remove(start_port); // Add all ports but remove the local to avoid loop!
            ArrayList<Node> result = TransferFuncFactory.findReachabilityByPropagation(network.getNTF(),
                    network.getTTF(), Pkt, Ports);
            allPath.addAll(result);
            logger.info("Node " + start_port + "->*, path num: " + result.size());

            for (Node n : result) {
                ArrayList<Rule> appliedRules = n.getRuleHistory();
                ArrayList<Integer> appliedPorts = n.getVisits();
                visitedRules.addAll(appliedRules);
                visitedPorts.addAll(appliedPorts);
                unvisitedRules.removeAll(appliedRules);
                unvisitedPorts.removeAll(appliedPorts);
                // logger.info("Before we have ports " + unvisitedEndPorts.size());
                unvisitedEndPorts.removeAll(appliedPorts);
                // logger.info("After we have ports " + unvisitedEndPorts.size());
            }
            visitedPorts.add(start_port);
            unvisitedPorts.remove(start_port);
            unvisitedEndPorts.remove(start_port);
            visitedStartPorts.add(start_port);

            pathNum += result.size();
        }
        // TODO: add directly the unvisited rules
        logger.info("We have unvisited #rule " + unvisitedRules.size());
        LinkedHashSet<Rule> waitRules = new LinkedHashSet<Rule>();
        waitRules.addAll(unvisitedRules);
        for (Rule aimRule : waitRules) {
            // Try to add the new rule here
            ArrayList<Integer> inPorts = aimRule.getInPorts();
            boolean match_path = false;
            for (Integer start_port : inPorts) {
                // It works only for HSA!
                Pkt = new Node();
                Pkt.setHdr(aimRule.getMatch()); // Directly set the header as the AIM
                Pkt.setPort(start_port);
                Node pNode = new Node(Pkt);
                HSATransFunc NTF = new HSATransFunc(network.getNTF());
                // HSATransFunc TTF = new HSATransFunc(network.getTTF());
                ArrayList<Node> nextHPs = NTF.T(pNode); // Get the nextHPs
                for (Node nxt_node : nextHPs) {
                    if (nxt_node.getRuleHistory().contains(aimRule)) {
                        // We have the new rule successfully
                        allPath.add(nxt_node);
                        pathNum += 1;

                        ArrayList<Rule> appliedRules = nxt_node.getRuleHistory();
                        ArrayList<Integer> appliedPorts = nxt_node.getVisits();
                        visitedRules.addAll(appliedRules);
                        visitedPorts.addAll(appliedPorts);
                        unvisitedRules.removeAll(appliedRules);
                        unvisitedPorts.removeAll(appliedPorts);
                        // logger.info("Before we have ports " + unvisitedEndPorts.size());
                        unvisitedEndPorts.removeAll(appliedPorts);
                        // logger.info("After we have ports " + unvisitedEndPorts.size());
                        match_path = true;
                        break;
                    }
                }
            }
            if (!match_path) {
                logger.info("The rule can not be matched with id " + aimRule.getId());
            }
        }
        long end = System.nanoTime();
        logger.info("We have " + "path# " + allPath.size() + " to cover all rules" + " cover/uncover "
                + visitedRules.size() + "/" + unvisitedRules.size() + " rules and "
                + visitedPorts.size() + "/" + unvisitedPorts.size() + " ports");
        logger.info("We run the program with " + (end - start) / 1000 + " us");

        for (int mode = 0; mode < 2; mode++) {
            ArrayList<Node> lastResult = allPath;
            ArrayList<Node> reducedResult = new ArrayList<Node>();

            // Reduce all paths in the last result
            visitedRules = new LinkedHashSet<Rule>();
            visitedPorts = new LinkedHashSet<Integer>();

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
    }

}
