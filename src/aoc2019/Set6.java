package aoc2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Set6 {
	public static void main(String[] args) throws IOException {
		Map<String, Ois> oiss = new HashMap<>();
		oiss.put("COM", new Ois("COM", null));

		for (String line : Files.readAllLines(Paths.get(".", "Set6.txt"))) {
			String[] split = line.trim().split("\\)");
			String orbits = split[0];
			String ois = split[1];
			oiss.put(ois, new Ois(ois, orbits));
		}

//		oiss.clear();
//		oiss.put("COM", new Ois("COM", null));
//		oiss.put("B", new Ois("B", "COM"));
//		oiss.put("C", new Ois("C", "B"));
//		oiss.put("D", new Ois("D", "C"));
//		oiss.put("E", new Ois("E", "D"));
//		oiss.put("F", new Ois("F", "E"));
//		oiss.put("G", new Ois("G", "B"));
//		oiss.put("H", new Ois("H", "G"));
//		oiss.put("I", new Ois("I", "D"));
//		oiss.put("J", new Ois("J", "E"));
//		oiss.put("K", new Ois("K", "J"));
//		oiss.put("L", new Ois("L", "K"));
//		oiss.put("YOU", new Ois("YOU", "K"));
//		oiss.put("SAN", new Ois("SAN", "I"));

//		oiss.values().stream().forEach(ois -> ois.resolve(oiss));
		for (Ois ois : oiss.values()) {
			if ("COM".equals(ois.name)) {
				continue;
			}
			oiss.get(ois.orbits).addOrbitedBy(ois.name);
		}

		// count
		long count = 0;
		for (Ois ois : oiss.values()) {
			count += ois.countOrbits(oiss);
		}
		System.out.println(count - 1);

		Ois you = oiss.get("YOU");
		Ois san = oiss.get("SAN");
		System.out.println(you.find(san, oiss) - 1);
	}

	
	private static class Ois { // Object in Space
		private final String name;
		private final String orbits; // null is COM
		private final HashSet<String> orbitedBy = new HashSet<>();
//		private Ois ptr = null;

		Ois(String name, String orbits) {
			this.name = name;
			this.orbits = orbits;
		}

		void addOrbitedBy(String ois) {
			orbitedBy.add(ois);
		}

		public long countOrbits(Map<String, Ois> oiss) {
			String next = orbits;
			long cnt = 1;
			while (!"COM".equals(next) && next != null) {
				next = oiss.get(next).orbits;
				cnt++;
			}
			return cnt;
		}

		void resolve(Map<String, Ois> oiss) {
			if ("COM".equals(orbits)) {
				return;
			}

//			ptr = oiss.get(orbits);
		}

		long find(Ois toFind, Map<String, Ois> oiss) {
			int curr = 0;
			HashSet<String> searched = new HashSet<>();

			if (orbitedBy.contains(toFind.name)) {
				return 0;
			}
			searched.add(name);

			HashSet<String> nextToSearch = new HashSet<>(orbitedBy);
			nextToSearch.add(orbits);

			do {
				curr++;
				HashSet<String> next = new HashSet<>();
				for (String orbiting : nextToSearch) {
					Ois orbitingOis = oiss.get(orbiting);
					if (orbitingOis.orbitedBy.contains(toFind.name)) {
						return curr;
					}
					searched.add(orbiting);
					next.addAll(orbitingOis.orbitedBy);
					if (orbitingOis.orbits != null) {
						next.add(orbitingOis.orbits);
					}
					next.removeAll(searched);
				}
				nextToSearch = next;
			} while (!nextToSearch.isEmpty());

			throw new IllegalStateException("No path found");
		}
	}
}
