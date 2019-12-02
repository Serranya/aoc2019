package aoc2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Set2 {

	public static void main(String[] args) throws IOException {
		Map<Long, Long> memory = new HashMap<>();
		long pos = 0;
		String[] vals = Files.readString(Paths.get(".", "Set2.txt")).split(",");
		for (String s : vals) {
			memory.put(pos++, Long.parseLong(s.trim()));
		};

		outer: for (long j = 0; j < 99; j++) {
			for (long k = 0; k < 99; k++) {
				Map<Long, Long> newMemory = new HashMap<>(memory);
				newMemory.put(1L, j); // nount
				newMemory.put(2L, k); // verb
				try {
					Long ret = run(newMemory);
					if (ret == 19690720) {
						System.out.println("Noun: " + j + " Verb: " + k);
						break outer;
					}
				} catch (IllegalStateException ignore) {}
			}
		}
	}

	private static Long run(Map<Long, Long> memory) {
		long ip = 0;
		while (true) {
			long opCode = memory.get(ip);
			if (opCode == 1) {
				long paramA = Objects.requireNonNull(memory.get(ip + 1));
				long paramB = Objects.requireNonNull(memory.get(ip + 2));
				long paramC = Objects.requireNonNull(memory.get(ip + 3));
				long a = Objects.requireNonNull(memory.get(paramA));
				long b = Objects.requireNonNull(memory.get(paramB));
				memory.put(paramC, a + b);
				ip += 4;
			} else if (opCode == 2) {
				long paramA = Objects.requireNonNull(memory.get(ip + 1));
				long paramB = Objects.requireNonNull(memory.get(ip + 2));
				long paramC = Objects.requireNonNull(memory.get(ip + 3));
				long a = Objects.requireNonNull(memory.get(paramA));
				long b = Objects.requireNonNull(memory.get(paramB));
				memory.put(paramC, a * b);
				ip += 4;
			} else if (opCode == 99) {
				break;
			} else {
				throw new IllegalStateException("Invalid opcode " + opCode + " at pos " + ip + "|" +  memory.get(0L));
			}
		}
		return memory.get(0L);
	}
}
