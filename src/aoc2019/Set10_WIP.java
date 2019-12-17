package aoc2019;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Integer.signum;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.math.MathContext.DECIMAL128;

public class Set10_WIP {
	public static void main(String[] args) throws IOException {
		Set<Asteroid> asteroids = new HashSet<>();

		int x = 0;
		int y = 0;
		for (String line : Files.readAllLines(Paths.get(".", "Set10.txt"))) {
			for (char c : line.toCharArray()) {
				if (c == '#') {
					asteroids.add(new Asteroid(x, y));
				}
				x++;
			}
			x = 0;
			y++;
		}
		asteroids.forEach(a -> a.countObservables(asteroids));
		asteroids.forEach(System.out::println);
		final Asteroid bestPos = asteroids.stream()
			.max((a1, a2) -> Comparator.<Asteroid>comparingInt(a -> a.obeservable.size()).compare(a1, a2))
			.orElseThrow();
		System.out.println("Best pos: " + bestPos);

		Set<Asteroid> others = asteroids.stream().filter(a -> !a.equals(bestPos)).collect(Collectors.toSet());
		int dir = 0; // 0=r,1=d,2=left,3=up
		int limitX = bestPos.x;
		int limitY = 1;
		int cnt = 1;
		Set<Asteroid> vapSet = new HashSet<>();
		boolean turnCompleted = false;
		while (others.size() > 1) { // start vaporizing
			// still completetly wrong. Rotating is broken
			if (turnCompleted) {
				vapSet.clear();
				turnCompleted = false;
			}
			Asteroid toBeVaporized = null;
			if (dir == 0) {
				toBeVaporized = findBestRight(others, bestPos, limitX, vapSet);
				if (toBeVaporized == null) {
					limitX++;
					if (limitX > x) {
						dir = 1;
						System.out.println("Dir change to down");
						continue;
					}
				}
			} else if (dir == 1) {
				toBeVaporized = findBestDown(others, limitY, bestPos.x);
				if (toBeVaporized == null) {
					dir = 2;
					System.out.println("Dir change to left");
					break;
				}
				limitY = toBeVaporized.y + 1;
				limitX = Integer.MAX_VALUE;
			} else if (dir == 2) {
				toBeVaporized = findBestLeft(others, limitX, bestPos.y);
				if (toBeVaporized == null) {
					dir = 3;
					System.out.println("Dir change to up");
					continue;
				}
				limitX = toBeVaporized.x - 1;
			}
			System.out.println("Vaporizing: " + cnt + " " + toBeVaporized);
			vapSet.add(toBeVaporized);
			cnt++;
		}
	}

	// x >= minX
	// y < origin.y
	private static Asteroid findBestRight(Iterable<Asteroid> asteroids, Asteroid origin, int minX, Set<Asteroid> vapSet) {
		Asteroid ret = null;
		for (Asteroid a : asteroids) {
			if (vapSet.contains(a)) {
				continue;
			}
			if (a.x >= minX && a.y < origin.y) {
				if (origin.hasLineOfSightTo(a, asteroids)) {
					if (ret == null) {
						ret = a;
					} else if (a.x < ret.x) {
						ret = a;
					}
				}
			}
		}
		return ret;
	}

	private static Asteroid findBestDown(Iterable<Asteroid> asteroids, int minY, int minX) {
		Asteroid ret = null;
		return ret;
	}

	private static Asteroid findBestLeft(Set<Asteroid> asteroids, int maxX, int minY) {
		Asteroid ret = null;
		return ret;
	}

	private static class Asteroid {
		final int x;
		final int y;
		final Set<Asteroid> obeservable = new HashSet<>();

		public Asteroid(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public boolean hasLineOfSightTo(Asteroid a, Iterable<Asteroid> asteroids) {
			int[] aMovementVec = movementVec(a);
			for (Asteroid other : asteroids) {
				if (other.equals(this) || other.equals(a)) {
					continue;
				}
				int[] otherMovementVec = movementVec(other);
				boolean isOnLine = false;
				//TODO edge cases
				if (min(abs(aMovementVec[0]), abs(otherMovementVec[0])) == 0) {
					if (max(abs(aMovementVec[0]), abs(otherMovementVec[0])) == 0) {
						isOnLine = true;
					} else {
						continue;
					}
				}
				if (!isOnLine && min(abs(aMovementVec[1]), abs(otherMovementVec[1])) == 0) {
					if (max(abs(aMovementVec[1]), abs(otherMovementVec[1])) == 0) {
						isOnLine = true;
					} else {
						continue;
					}
				}

				if (!isOnLine) {
					BigDecimal divisor1 =
							BigDecimal.valueOf(aMovementVec[0]).divide(BigDecimal.valueOf(otherMovementVec[0]), DECIMAL128);
					BigDecimal divisor2 =
							BigDecimal.valueOf(aMovementVec[1]).divide(BigDecimal.valueOf(otherMovementVec[1]), DECIMAL128);
					isOnLine = divisor1.compareTo(divisor2) == 0;
				}
				if (isOnLine) {
					boolean isOtherSide = false;
					isOtherSide = signum(aMovementVec[0]) != signum(otherMovementVec[0])
							|| signum(aMovementVec[1]) != signum(otherMovementVec[1]);
					if (isOtherSide) {
						continue;
					}
					// other ist näher auf der x oder y axe an this
					if (abs(otherMovementVec[0]) < abs(aMovementVec[0]) || abs(otherMovementVec[1]) < abs(aMovementVec[1])) {
						return false;
					}
				}
			}
			return true;
		}

		int[] movementVec(Asteroid other) {
			int xMovement = other.x - x;
			int yMovement = other.y - y;
			return new int[] {xMovement, yMovement};
		}

		void countObservables(Iterable<Asteroid> others) {
			for (Asteroid other : others) {
				if (other.equals(this)) {
					continue;
				}

				boolean isObservable = true;
				int[] moveVec1 = movementVec(other);
				for (Asteroid alreadyObserved : obeservable) {
					int[] moveVec2 = movementVec(alreadyObserved);
					if (min(abs(moveVec1[0]), abs(moveVec2[0])) == 0) {
						if (max(abs(moveVec1[0]), abs(moveVec2[0])) == 0) {
							boolean isOtherSide = false;
							isOtherSide = signum(moveVec1[0]) != signum(moveVec2[0])
									|| signum(moveVec1[1]) != signum(moveVec2[1]);
							if (!isOtherSide) {
								isObservable = false;
								break;
							} else {
								continue;
							}
						} else {
							continue;
						}
					}
					if (min(abs(moveVec1[1]), abs(moveVec2[1])) == 0) {
						if (max(abs(moveVec1[1]), abs(moveVec2[1])) == 0) {
							boolean isOtherSide = false;
							isOtherSide = signum(moveVec1[0]) != signum(moveVec2[0])
									|| signum(moveVec1[1]) != signum(moveVec2[1]);
							if (!isOtherSide) {
								isObservable = false;
								break;
							} else {
								continue;
							}
						} else {
							continue;
						}
					}

					// if moveVec & moveVec2 have common divisor
					BigDecimal divisor1 = BigDecimal.valueOf(moveVec1[0]).divide(BigDecimal.valueOf(moveVec2[0]), DECIMAL128);
					BigDecimal divisor2 = BigDecimal.valueOf(moveVec1[1]).divide(BigDecimal.valueOf(moveVec2[1]), DECIMAL128);
					if (divisor1.compareTo(divisor2) == 0) {
						// aha. But maybe its on the other side. in that case it' ok
						boolean isOtherSide = false;
						isOtherSide = signum(moveVec1[0]) != signum(moveVec2[0])
								|| signum(moveVec1[1]) != signum(moveVec2[1]);
						if (!isOtherSide) {
							isObservable = false;
							break;
						}
					}
				}

				if (isObservable) {
					obeservable.add(other);
				}
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Asteroid other = (Asteroid) obj;
			return x == other.x && y == other.y;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Asteroid [x=").append(x).append(", y=").append(y).append(", obeservable=")
					.append(obeservable.size()).append("]");
			return builder.toString();
		}
	}
}
