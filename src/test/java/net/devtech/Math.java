package net.devtech;

import org.bukkit.Bukkit;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class Math {
	public static void main(String[] args) {
		System.out.println(findDistanceTraveled(0, 12));
		System.out.println(findDistance(0, 12));
		Bukkit.createInventory()
	}


	public static double findVelocityAfterRecursive(double init, int time) {
		if (time == 0) return init;
		return findVelocityAfterRecursive(init * .98 - .0784, --time);
	}

	public static double findVelocityAfter(double init, int time) {
		return (3.92 + init) * pow(.98, time) - 3.92;
	}

	public static double findDistance(double init, int time) {
		return -194.033*init*pow(0.98,time) - 3.92*time + 35.853729850040814;
	}

	public static double findInitialVelocity(int time, double after) {
		return (after / (pow(.98, time) - 3.92)) - 3.92;
	}

	public static double findDistanceTraveled(double initVel, double time) {
		if (time < 1) return 0;
		return initVel + findDistanceTraveled((initVel - .08D) * .98, time - 1);
	}

	private static boolean fuzzyEquals(double a, double b) {
		return a == b || abs(a - b) < 20;
	}

}
