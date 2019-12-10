package aoc2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Set8 {
	public static void main(String[] args) throws IOException {
		char[] in = Files.readString(Paths.get(".", "Set8.txt")).trim().toCharArray();
		int imgSize = 25 * 6;
		ArrayList<ArrayList<Integer>> assembled = new ArrayList<>(imgSize);
		int[] currLayer = new int[imgSize];
		int curr = 0;
		for (int i = 0; i < imgSize; i++) {
			assembled.add(new ArrayList<>());
		}

		int fewestZeroDigits = Integer.MAX_VALUE;
		int checkSum = -1;
		while (curr < in.length) {
			currLayer[curr % currLayer.length] = in[curr] - '0';
			assembled.get(curr % currLayer.length).add(in[curr] - '0');
			curr++;
			if (curr != 0 && curr % currLayer.length == 0) {
				int zeros = 0;
				int ones = 0;
				int twos = 0;
				for (int i : currLayer) {
					if (i == 0) {
						zeros++;
					} else if (i == 1) {
						ones++;
					} else if (i == 2) {
						twos++;
					}
				}
				if (zeros < fewestZeroDigits) {
					fewestZeroDigits = zeros;
					checkSum = ones * twos;
				}
			}
		}
		System.out.println(checkSum);

		for (int i = 0; i < 6 ; i++) {
			for (int j = 0; j < 25; j++) {
				ArrayList<Integer> pixelDesc = assembled.get((i * 25) + j);
				for (Integer pix : pixelDesc) {
					if (pix != 2) {
						System.out.print(pix == 0 ? ' ' : 'x');
						break;
					}
				}
			}
			System.out.println();
		}
	}
}
