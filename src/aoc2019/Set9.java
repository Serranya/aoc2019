package aoc2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;

public class Set9 {
	public static void main(String[] args) throws IOException {
		long[] memory =
				Arrays.asList(Files.readString(Paths.get(".", "Set9.txt")).trim().split(","))
				.stream()
				.mapToLong(Long::parseLong)
				.toArray();
		new Computer(memory, () -> 2L).run();
	}

	private static class Computer {
		private final Instruction[] instructions = new Instruction[100];
		private final Map<Long, Long> memory;
		private final LongSupplier input;
		private long ip = 0;
		private long relativeBase = 0;

		Computer(long[] memory, LongSupplier input) {
			this.memory = new HashMap<>(memory.length);
			long pos = 0;
			for (long l : memory) {
				this.memory.put(pos++, l);
			}
			this.input = input;
			instructions[1] = new AddInstruction();
			instructions[2] = new MulInstruction();
			instructions[3] = new SaveInstruction();
			instructions[4] = new WriteInstruction();
			instructions[5] = new JumpIfTrueInstruction();
			instructions[6] = new JumpIfFalseInstruction();
			instructions[7] = new LessThanInstruction();
			instructions[8] = new EqualInstruction();
			instructions[9] = new SetRelativeBaseInstruction();
			instructions[99] = new HaltInstruction();
		}

		long run() {
			while (true) {
				OpCode opCode = new OpCode(memory.get(ip));
				int opId = opCode.id();
				Instruction instruction = instructions[opId];
				Result result = instruction.exec(ip, memory, opCode, input, relativeBase);
				if (result == Result.JUMP) {
					ip = instruction.value();
				} else {
					ip += instruction.size();
				}
				if (result == Result.SET_RELATIVE_BASE) {
					relativeBase += instruction.value();
				}
				if (result == Result.HALT) {
					break;
				}
			}
			return memory.get(0L);
		}
	}

	private static class OpCode {
		private final long def;

		OpCode(long def) {
			this.def = def;
		}

		int id() {
			return (int) (def % 100);
		}

		ParameterMode modeFor(int argNum) {
			long modeCode = def;
			for (int i = 0; i < argNum + 2; i++) {
				modeCode /= 10;
			}
			return modeCode % 10 == 0 ? ParameterMode.POSITION :
				    modeCode % 10 == 1 ? ParameterMode.IMMEDIATE :
				    ParameterMode.RELATIVE;
		}
	}

	private enum ParameterMode {
		POSITION, IMMEDIATE, RELATIVE
	}

	enum Result {
		CONTINUE, HALT, JUMP, SET_RELATIVE_BASE;
	}

	interface Instruction {
		Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase);
		int size();
		default long value() {throw new UnsupportedOperationException("Not implemented");}
	}

	private static class AddInstruction implements Instruction {
		@Override
		public int size() {
			return 4;
		}

		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getValueParam(0);
			long paramB = params.getValueParam(1);
			long paramC = params.getAddressParam(2);
			memory.put(paramC, paramA + paramB);
			return Result.CONTINUE;
		}
	}

	private static class MulInstruction implements Instruction {
		@Override
		public int size() {
			return 4;
		}

		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getValueParam(0);
			long paramB = params.getValueParam(1);
			long paramC = params.getAddressParam(2);
			memory.put(paramC, paramA * paramB);
			return Result.CONTINUE;
		}
	}

	private static class HaltInstruction implements Instruction {
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			return Result.HALT;
		}

		@Override
		public int size() {
			return 1;
		}
	}

	private static class SaveInstruction implements Instruction {
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getAddressParam(0);
			memory.put(paramA, input.getAsLong());
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 2;
		}
	}

	private static class WriteInstruction implements Instruction {
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getValueParam(0);
			System.out.println(paramA);
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 2;
		}
	}

	private static class JumpIfTrueInstruction implements Instruction {
		private long target = -1;
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getValueParam(0);

			if (paramA != 0) {
				target = params.getValueParam(1);
				return Result.JUMP;
			}
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 3;
		}

		@Override
		public long value() {
			return target;
		}
	}

	private static class JumpIfFalseInstruction implements Instruction {
		private long target = -1;
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getValueParam(0);

			if (paramA == 0) {
				target = params.getValueParam(1);
				return Result.JUMP;
			}
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 3;
		}

		@Override
		public long value() {
			return target;
		}
	}

	private static class LessThanInstruction implements Instruction {
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getValueParam(0);
			long paramB = params.getValueParam(1);
			long paramC = params.getAddressParam(2);

			memory.put(paramC, paramA < paramB ? 1L : 0L);
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 4;
		}
	}

	private static class EqualInstruction implements Instruction {
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			long paramA = params.getValueParam(0);
			long paramB = params.getValueParam(1);
			long paramC = params.getAddressParam(2);

			memory.put(paramC, paramA == paramB ? 1L : 0L);
			return Result.CONTINUE;
		}

		@Override
		public int size() {
			return 4;
		}
	}

	private static class SetRelativeBaseInstruction implements Instruction {
		private long value;
		@Override
		public Result exec(long ip, Map<Long, Long> memory, OpCode opCode, LongSupplier input, long relativeBase) {
			var params = new Parameters(memory, opCode, ip, relativeBase);
			value = params.getValueParam(0);
			return Result.SET_RELATIVE_BASE;
		}

		@Override
		public long value() {
			return value;
		}

		@Override
		public int size() {
			return 2;
		}
	}

	private static class Parameters {
		private final Map<Long, Long> memory;
		private final OpCode opCode;
		private final long ip;
		private final long relativeBase;

		Parameters(Map<Long, Long> memory, OpCode opCode, long ip, long relativeBase) {
			this.memory = memory;
			this.opCode = opCode;
			this.ip = ip;
			this.relativeBase = relativeBase;
		}

		long getValueParam(int paramNum) {
			long param = memory.get(ip + paramNum + 1);
			ParameterMode mode = opCode.modeFor(paramNum);
			switch (mode) {
			case IMMEDIATE:
				return param;
			case POSITION:
				return memory.getOrDefault(param, 0L);
			case RELATIVE:
				return memory.get(relativeBase + param);
			}
			throw new IllegalStateException();
		}

		long getAddressParam(int paramNum) {
			long param = memory.get(ip + paramNum + 1);
			ParameterMode mode = opCode.modeFor(paramNum);
			switch (mode) {
			case IMMEDIATE:
				throw new IllegalStateException();
			case POSITION:
				return param;
			case RELATIVE:
				return relativeBase + param;
			}
			throw new IllegalStateException();
		}
	}
}
