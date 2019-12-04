package aoc2019;

import java.util.stream.IntStream;

public class Set4 {
	public static void main(String[] args) {
		System.out.println(isValid("112233".toCharArray()));
		System.out.println(isValid("123444".toCharArray()));
		System.out.println(isValid("111122".toCharArray()));
		long matches = IntStream.rangeClosed(256310, 732736)
				.mapToObj(Integer::toString)
				.map(String::toCharArray)
				.filter(Set4::isValid)
				.count();
		System.out.println(matches);
	}

	static boolean isValid(char[] in) {
		char currChar = in[0];
		int cnt = 1;
		boolean hasPair = false;
		for (int i = 1; i < in.length; i++) {
			if (in[i] == currChar) {
				cnt += 1;
			} else {
				currChar = in[i];
				hasPair |= cnt == 2;
				cnt = 1;
			}
			if (in[i -1] > in[i]) {
				return false;
			}
		}
		return hasPair || cnt == 2;
	}
}
