package chengjun;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.logging.*;

import utils.Save;
import utils.Gephi;
import bean.Network;
import bean.basis.Node;
import bean.basis.Rule;

import factory.HeaderFactory;
import factory.TransferFuncFactory;
import apverifier.bean.APVTransFunc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

// ATPG algorithm
public class atpg {
	public static void main(String args[]) throws SecurityException, IOException {
		Logger logger = Logger.getLogger("Chengjun");
		logger.setLevel(Level.INFO);
		FileHandler fh = new FileHandler("log_run.log");
		logger.addHandler(fh);
		fh.setFormatter(new SimpleFormatter());

		Network network = new Network();
		ArrayList<String> networksName = new ArrayList<String>();
		networksName.add("simple_stanford.network");
		networksName.add("2_4_4.network");
		networksName.add("4_8_16.network");
		Path path = Paths.get("examples", networksName.get(0));
		network.importFromFile(path.toString());

		Node Pkt = new Node();
		// 32 bit for StanfordSimple, 34 bit for fattree
		Pkt.setHdr(HeaderFactory.generateInputHeader(32, 'x'));
		// Pkt.setHdr(HeaderFactory.generateHeader(APVTransFunc.predicates.size()));

		// Get all ports here
		ArrayList<Rule> linkRules = network.getTTF().rules;
		LinkedHashSet<Integer> netPortSet = new LinkedHashSet<Integer>();
		for (Rule rule : linkRules) {
			for (Integer p : rule.getInPorts()) {
				netPortSet.add(p);
			}
			for (Integer p : rule.getOutPorts()) {
				netPortSet.add(p);
			}
		}
		ArrayList<Integer> netPortList = new ArrayList<>(netPortSet);
		logger.info("network ports number is " + netPortList.size());

		// ArrayList<Node>
		for (int i = 0; i < netPortList.size(); i++) {
			Pkt.setPort(netPortList.get(i));
			ArrayList<Integer> Ports = new ArrayList<Integer>();

			Ports.addAll(netPortList);
			Ports.remove(i); // Add all ports but remove the local to avoid loop!

			ArrayList<Node> result = TransferFuncFactory.findReachabilityByPropagation(network.getNTF(),
					network.getTTF(), Pkt, Ports);
			logger.info("From " + netPortList.get(i) + " to *, path num: " + result.size());
			if (result.size() > 0 && result.get(0).toString().length() < 1000) {
				logger.info("Print result for 0 is \n" + result.get(0).toString());
				// logger.info(result.get(0).getHsHistory());
				logger.info("=============");
			}
		}
	}
}
