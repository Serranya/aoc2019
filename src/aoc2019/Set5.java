package aoc2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.IntSupplier;

public class Set5 {
	public static void main(String[] args) throws IOException {
		int[] memory = Arrays.asList(Files.readString(Paths.get(".", "Set5.txt")).split(",")).stream()
			.map(String::trim)
			.mapToInt(Integer::parseInt)
			.toArray();
//		int[] memory = {1002,4,3,4,33};
		System.out.println(new Computer(memory, () -> 1).run());
	}

	private static class Computer {
		private final Instruction[] instructions = new Instruction[100];
		private final int[] memory;
		private final IntSupplier input;
		private int ip = 0;

		Computer(int[] memory, IntSupplier input) {
			this.memory = memory;
			this.input = input;
			instructions[1] = new AddInstruction();
			instructions[2] = new MulInstruction();
			instructions[3] = new SaveInstruction();
			instructions[4] = new WriteInstruction();
			instructions[99] = new HaltInstruction();
		}

		int run() {
			while (true) {
				OpCode opCode = new OpCode(memory[ip]);
				int opId = opCode.id();
				Instruction instruction = instructions[opId];
				Result result = instruction.exec(ip, memory, opCode, input);
				ip += instruction.size();
				if (result == Result.HALT) {
					break;
				}
			}
			return memory[0];
		}
	}

	private static class OpCode {
		private final int def;

		OpCode(int def) {
			this.def = def;
		}

		int id() {
			return def % 100;
		}

		ParameterMode modeFor(int argNum) {
			int modeCode = def;
			for (int i = 0; i < argNum + 2; i++) {
				modeCode /= 10;
			}
			return modeCode % 10 == 0 ? ParameterMode.POSITiON : ParameterMode.IMMEDIATE;
		}
	}

	private enum ParameterMode {
		POSITiON, IMMEDIATE
	}

	enum Result {
		CONTINUE, HALT;
	}

	interface Instruction {
		Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input);
		int size();
	}

	private static class AddInstruction implements Instruction {
		@Override
		public int size() {
			return 4;
		}

		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			int paramA = memory[ip + 1];
			int paramB = memory[ip + 2];
			int paramC = memory[ip + 3];
			if (opCode.modeFor(0) == ParameterMode.POSITiON) {
				paramA = memory[paramA];
			}
			if (opCode.modeFor(1) == ParameterMode.POSITiON) {
				paramB = memory[paramB];
			}
			memory[paramC] = paramA + paramB;
			return Result.CONTINUE;
		}
	}

	private static class MulInstruction implements Instruction {
		@Override
		public int size() {
			return 4;
		}

		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			int paramA = memory[ip + 1];
			int paramB = memory[ip + 2];
			int paramC = memory[ip + 3];
			if (opCode.modeFor(0) == ParameterMode.POSITiON) {
				paramA = memory[paramA];
			}
			if (opCode.modeFor(1) == ParameterMode.POSITiON) {
				paramB = memory[paramB];
			}
			memory[paramC] = paramA * paramB;
			return Result.CONTINUE;
		}
	}

	private static class HaltInstruction implements Instruction {
		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			return Result.HALT;
		}

		@Override
		public int size() {
			return 1;
		}
	}

	private static class SaveInstruction implements Instruction {
		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			memory[memory[ip + 1]] = input.getAsInt();
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 2;
		}
	}

	private static class WriteInstruction implements Instruction {
		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			int paramA = memory[ip + 1];
			if (opCode.modeFor(0) == ParameterMode.POSITiON) {
				paramA = memory[paramA];
			}
			System.out.println(paramA);
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 2;
		}
	}
}
