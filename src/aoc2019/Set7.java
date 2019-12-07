package aoc2019;

import java.nio.file.Paths;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class Set7 {
	public static void main(String[] args) throws IOException, InterruptedException {
//		task1();
		task2();
	}

	static void task1() throws IOException {
		Collection<Integer[]> permutations = permutations(new int[] {0, 1, 2, 3, 4});
		int[] memory = Arrays.asList(Files.readString(Paths.get("." , "Set7.txt")).split(","))
				.stream()
				.map(String::trim)
				.mapToInt(Integer::parseInt)
				.toArray();

		long max = Long.MIN_VALUE;
		int[] tmpMemory = new int[memory.length];
		ThreadLocal<Integer> input = ThreadLocal.withInitial(() -> 0);
		for (Integer[] phases : permutations) {
			for (int phase : phases) {
				System.arraycopy(memory, 0, tmpMemory, 0, memory.length);
				Iterator<Integer> intIter = Arrays.asList(phase, input.get()).iterator();
				new Computer(tmpMemory, () -> intIter.next(), input::set).run();
			}
			max = Math.max(max, input.get());
			input.set(0);
		}
		System.out.println(max);
	}

	static void task2() throws IOException, InterruptedException {
		Collection<Integer[]> permutations = permutations(new int[] {5, 6, 7, 8, 9});
		int[] memory = Arrays.asList(Files.readString(Paths.get("." , "Set7.txt")).split(","))
				.stream()
				.map(String::trim)
				.mapToInt(Integer::parseInt)
				.toArray();

//		memory =
//				Arrays.asList(
//						"3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5"
//						.split(",")
//				).stream()
//				.map(String::trim)
//				.mapToInt(Integer::parseInt)
//				.toArray();

		long max = Long.MIN_VALUE;
//		permutations = Collections.singleton(new Integer[] {9,8,7,6,5});
		for (Integer[] phases : permutations) {
			ComputerConnection c1toc2 = new ComputerConnection();
			ComputerConnection c2toc3 = new ComputerConnection();
			ComputerConnection c3toc4 = new ComputerConnection();
			ComputerConnection c4toc5 = new ComputerConnection();
			ComputerConnection c5toc1 = new ComputerConnection();
			Thread c1 = new Thread(new ComputerRunnable("C1", phases[0], 0, memory, c1toc2::write, c5toc1::read));
			c1.setName("C1");
			Thread c2 = new Thread(new ComputerRunnable("C2", phases[1], memory, c2toc3::write, c1toc2::read));
			c2.setName("C2");
			Thread c3 = new Thread(new ComputerRunnable("C3", phases[2], memory, c3toc4::write, c2toc3::read));
			c3.setName("C3");
			Thread c4 = new Thread(new ComputerRunnable("C4", phases[3], memory, c4toc5::write, c3toc4::read));
			c4.setName("C4");
			Thread c5 = new Thread(new ComputerRunnable("C5", phases[4], memory, c5toc1::write, c4toc5::read));
			c5.setName("C5");


			c1.start();
			c2.start();
			c3.start();
			c4.start();
			c5.start();

			c1.join();
			c2.join();
			c3.join();
			c4.join();
			c5.join();
			max = Math.max(max, c5toc1.read());
//			break;
		}
		System.out.println(max);
	}


	private static Collection<Integer[]> permutations(int[] ints) {
		ArrayList<Integer[]> perms = new ArrayList<>(5 * 4 * 3 * 2 * 1);

		for (int firstInt : ints) {
			for (int secondInt : ints) {
				if (secondInt == firstInt) continue;
				for (int thirdInt : ints) {
					if (thirdInt == secondInt || thirdInt == firstInt) continue;
					for (int forthInt : ints) {
						if (forthInt == thirdInt || forthInt == secondInt || forthInt == firstInt) continue;
						for (int lastInt : ints) {
							if (lastInt == firstInt || lastInt == secondInt || lastInt == thirdInt || lastInt == forthInt) continue;
							perms.add(new Integer[] {firstInt, secondInt, thirdInt, forthInt, lastInt});
						}
					}
				}
			}
		}
		return perms;
	}

	private static class ComputerRunnable implements Runnable {
		private final String name;
		private final int phase;
		private Integer initialInput;
		private final int[] memory;
		private final IntConsumer output;
		private final IntSupplier input;

		public ComputerRunnable(String name, int phase, Integer initialInput, int[] memory, IntConsumer output, IntSupplier input) {
			this.name = name;
			this.phase = phase;
			this.initialInput = initialInput;
			this.memory = new int[memory.length];
			System.arraycopy(memory, 0, this.memory, 0, memory.length);
			this.output = output;
			this.input = input;
		}

		public ComputerRunnable(String name, int phase, int[] memory, IntConsumer output, IntSupplier input) {
			this(name, phase, null, memory, output, input);
		}

		@Override
		public void run() {
			ThreadLocal<Integer> phase = new ThreadLocal<>();
			phase.set(this.phase);
			new Computer(memory, () -> {
//				System.out.println(name + " asking for input");
				if (phase.get() != null) {
					int cpy = phase.get();
//					System.out.println(name + " input is the phase: " + cpy);
					phase.set(null);
					return cpy;
				}
				if (initialInput != null) {
					int cpy = initialInput;
//					System.out.println(name + " input is " + cpy + " (initial)");
					initialInput = null;
					return cpy;
				}

				int cpy = input.getAsInt();
//				System.out.println(name + " input is: " + cpy);
				return cpy;
			}, output).run();
		}
	};

	private static class ComputerConnection {
		private final LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(1);
		public void write(int in) {
			try {
				q.put(in);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		public int read() {
			try {
				return q.take();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class Computer {
		private final Instruction[] instructions = new Instruction[100];
		private final int[] memory;
		private final IntSupplier input;
		private int ip = 0;

		Computer(int[] memory, IntSupplier input, IntConsumer output) {
			this.memory = memory;
			this.input = input;
			instructions[1] = new AddInstruction();
			instructions[2] = new MulInstruction();
			instructions[3] = new SaveInstruction();
			instructions[4] = new WriteInstruction(output);
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
		private final IntConsumer output;

		WriteInstruction(IntConsumer output) {
			this.output = output;
		}

		@Override
		public Result exec(int ip, int[] memory, OpCode opCode, IntSupplier input) {
			int paramA = memory[ip + 1];
			if (opCode.modeFor(0) == ParameterMode.POSITiON) {
				paramA = memory[paramA];
			}
			output.accept(paramA);
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
