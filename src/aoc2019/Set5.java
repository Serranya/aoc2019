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
		new Computer(memory, () -> 5).run();
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
			instructions[5] = new JumpIfTrueInstruction();
			instructions[6] = new JumpIfFalseInstruction();
			instructions[7] = new LessThanInstruction();
			instructions[8] = new EqualInstruction();
			instructions[99] = new HaltInstruction();
		}

		int run() {
			while (true) {
				OpCode opCode = new OpCode(memory[ip]);
				int opId = opCode.id();
				Instruction instruction = instructions[opId];
				Result result = instruction.exec(ip, memory, opCode, input);
				if (result == Result.JUMP) {
					ip = instruction.jumpTarget();
				} else {
					ip += instruction.size();
				}
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
		CONTINUE, HALT, JUMP;
	}

	interface Instruction {
		Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input);
		int size();
		default int jumpTarget() {throw new UnsupportedOperationException("Not implemented");}
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

	private static class JumpIfTrueInstruction implements Instruction {
		private int target = -1;
		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			int paramA = memory[ip + 1];
			if (opCode.modeFor(0) == ParameterMode.POSITiON) {
				paramA = memory[paramA];
			}

			if (paramA != 0) {
				int paramB = memory[ip + 2];
				if (opCode.modeFor(1) == ParameterMode.POSITiON) {
					paramB = memory[paramB];
				}
				target = paramB;
				return Result.JUMP;
			}
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 3;
		}

		@Override
		public int jumpTarget() {
			return target;
		}
	}

	private static class JumpIfFalseInstruction implements Instruction {
		private int target = -1;
		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			int paramA = memory[ip + 1];
			if (opCode.modeFor(0) == ParameterMode.POSITiON) {
				paramA = memory[paramA];
			}

			if (paramA == 0) {
				int paramB = memory[ip + 2];
				if (opCode.modeFor(1) == ParameterMode.POSITiON) {
					paramB = memory[paramB];
				}
				target = paramB;
				return Result.JUMP;
			}
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 3;
		}

		@Override
		public int jumpTarget() {
			return target;
		}
	}

	private static class LessThanInstruction implements Instruction {
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

			memory[paramC] = paramA < paramB ? 1 : 0;
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 4;
		}
	}

	private static class EqualInstruction implements Instruction {
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

			memory[paramC] = paramA == paramB ? 1 : 0;
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 4;
		}
	}
}
