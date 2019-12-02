package aoc2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Set1 {
	public static void main(String[] args) throws IOException {
		Path p = Paths.get(".", "Set1.txt");
		long result = Files.lines(p)
			.mapToLong(Long::parseLong)
			.mapToDouble(l -> l / 3)
			.mapToLong(l -> (long) l)
			.map(l -> l - 2)
			.map(l -> {
				long extraFuel = ((long) (l / 3)) - 2;
				long addition = 0;
				while (extraFuel > 0) {
					addition += extraFuel;
					extraFuel = ((long) (extraFuel / 3)) - 2;
				};
				return l + addition;
			})
			.sum();
		System.out.println(result);
	}
}
