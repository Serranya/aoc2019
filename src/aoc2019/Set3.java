package aoc2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Set3 {
	public static void main(String[] args) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(".", "Set3.txt"));

		Wire w1 = buildWire(lines.get(0));
		Wire w2 = buildWire(lines.get(1));

//		Wire w1 = buildWire("R8,U5,L5,D3");
//		Wire w2 = buildWire("U7,R6,D4,L4");

//		Wire w1 = buildWire("R75,D30,R83,U83,L12,D49,R71,U7,L72");
//		Wire w2 = buildWire("U62,R66,U55,R34,D71,R55,D58,R83");

//		Wire w1 = buildWire("R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51");
//		Wire w2 = buildWire("U98,R91,D20,R16,D67,R40,U7,R15,U6,R7");

//		Set<Edge> both = new HashSet<>(w1.edges);
//		both.addAll(w2.edges);
//		System.out.println(new Wire(both).toString());

		Iterable<Point> intersections = w1.intersections(w2);
		intersections.forEach(System.out::println);
		System.out.println("Smallest mDist: " + smallestManhattenDistance(intersections));
	}

	private static Wire buildWire(String def) {
		Collection<Edge> edges = new ArrayList<>();
		Point orig = new Point(0, 0);

		for (String dir : def.split(",")) {
			Edge e = new Edge(orig, dir);
			edges.add(e);
			orig = e.end();
		}
		return new Wire(edges);
	}

	private static int smallestManhattenDistance(Iterable<Point> points) {
		Point origin = new Point(0, 0);
		int ret = Integer.MAX_VALUE;
		for (Point p : points) {
			if (p.equals(origin)) {
				continue;
			}
			ret = Math.min(ret, mDistance(p));
		}

		return ret;
	}

	private static int mDistance(Point b) {
		return Math.abs(b.x) + Math.abs(b.y);
	}

	private static class Point {
		private final int x;
		private final int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Point up(int dist) {
			return new Point(x +  dist, y);
		}

		public Point down(int dist) {
			return new Point(x - dist, y);
		}

		public Point left(int dist) {
			return new Point(x, y - dist);
		}

		public Point right(int dist) {
			return new Point(x, y + dist);
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			return x == other.x && y == other.y;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ")";
		}
	}

	private static class Edge {
		private final Point orig;
		private final String dir;

		Edge(Point orig, String dir) {
			this.orig = orig;
			this.dir = dir;
		}

		Point end() {
			int dist = dist();
			if (dir.startsWith("U")) {
				return orig.up(dist);
			} else if (dir.startsWith("D")) {
				return orig.down(dist);
			} else if (dir.startsWith("L")) {
				return orig.left(dist);
			} else {
				return orig.right(dist);
			}
		}

		Iterable<Point> intersections(Edge other) {
			Collection<Point> intersections = new HashSet<>();
			int dist = dist();
			for (int i = 0; i <= dist; i++) {
				Point p;
				if (dir.startsWith("U")) {
					p = orig.up(i);
				} else if (dir.startsWith("D")) {
					p = orig.down(i);
				} else if (dir.startsWith("L")) {
					p = orig.left(i);
				} else {
					p = orig.right(i);
				}
				if (other.intersects(p)) {
					intersections.add(p);
				}
			}
			return intersections;
		}

		boolean intersects(Point p) {
			Point end = end();
			if (p.x == orig.x) {
				return Math.min(orig.y, end.y) <= p.y && p.y <= Math.max(orig.y, end.y);
			} else if (p.y == orig.y) {
				return Math.min(orig.x, end.x) <= p.x && p.x <= Math.max(orig.x, end.x);
			} else {
				return false;
			}
		}

		private int dist() {
			return Integer.parseInt(dir.substring(1));
		}

		@Override
		public String toString() {
			return "[" + orig.toString() + " -> " + end().toString() + "]";
		}
	}

	private static class Wire {
		Set<Edge> edges = new HashSet<>();

		Wire(Iterable<Edge> edges) {
			edges.forEach(this.edges::add);
		}

		Iterable<Point> intersections(Wire other) {
			Set<Point> intersections = new HashSet<>();
			for (Edge e : edges) {
				other.intersections(e).forEach(intersections::add);
			}
			return intersections;
		}

		Iterable<Point> intersections(Edge other) {
			Set<Point> intersections = new HashSet<>();
			for (Edge e : edges) {
				e.intersections(other).forEach(intersections::add);
			}
			return intersections;
		}

		@Override
		public String toString() {
			int minX = 0;
			int maxX = 0;
			int minY = 0;
			int maxY = 0;
			for (Edge e : edges) {
				minX = Math.min(minX, Math.min(e.orig.x, e.end().x));
				maxX = Math.max(maxX, Math.max(e.orig.x, e.end().x));
				minY = Math.min(minY, Math.min(e.orig.y, e.end().y));
				maxY = Math.max(maxY, Math.max(e.orig.y, e.end().y));
			}
			StringBuilder sb = new StringBuilder();
			for (int x = maxX; x >= minX; x--) {
				y: for (int y = minY; y <= maxY; y++) {
					if (x == 0 && y == 0) {
						sb.append('O');
						continue;
					}
					for (Edge e : edges) {
						if (e.intersects(new Point(x, y))) {
							sb.append('x');
							continue y;
						}
					}
					sb.append('.');
				}
				sb.append("\n");
			}
			return sb.toString();
		}
	}
}
